package de.xahrie.trues.api.discord.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.IntStream;

import de.xahrie.trues.api.datatypes.calendar.TimeFormat;
import de.xahrie.trues.api.discord.builder.EmbedWrapper;
import de.xahrie.trues.api.discord.builder.InfoPanelBuilder;
import de.xahrie.trues.api.discord.builder.queryCustomizer.NamedQuery;
import de.xahrie.trues.api.discord.builder.queryCustomizer.SimpleCustomQuery;
import de.xahrie.trues.api.discord.command.InputHandler;
import de.xahrie.trues.api.discord.command.context.UseView;
import de.xahrie.trues.api.discord.command.slash.annotations.Msg;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.discord.user.DiscordUserFactory;
import de.xahrie.trues.api.util.Const;
import de.xahrie.trues.api.util.StringUtils;
import lombok.Data;
import lombok.experimental.ExtensionMethod;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;

@Data
@ExtensionMethod(StringUtils.class)
public abstract class Replyer {
  protected final Class<? extends IReplyCallback> clazz;
  protected final List<SimpleCustomQuery> customEmbedData = new ArrayList<>();
  protected String name;
  protected IReplyCallback event;
  protected boolean end = false;

  protected void addEmbed(NamedQuery namedQuery, Object... parameters) {
    customEmbedData.add(SimpleCustomQuery.params(namedQuery, Arrays.asList(parameters)));
  }

  protected void addEmbedData(NamedQuery namedQuery, List<Object[]> customData) {
    customEmbedData.add(SimpleCustomQuery.custom(namedQuery, customData));
  }

  protected void addEmbedData(NamedQuery namedQuery, Object[]... customData) {
    customEmbedData.add(SimpleCustomQuery.custom(namedQuery, Arrays.asList(customData)));
  }

  protected boolean reply(String message) {
    return reply(message, null);
  }

  protected boolean reply(String message, Boolean ephemeral) {
    try {
      WebhookMessageCreateAction<Message> msg = event.getHook().sendMessage(message);
      if (ephemeral != null) msg = msg.setEphemeral(ephemeral);
      msg.queue();
      end = true;
      return true;
    } catch (RejectedExecutionException ignored) {
      return false;
    }
  }

  protected Member getInvokingMember() {
    if (event == null) {
      reply("Internal Error");
      return null;
    }

    final Member invoker = event.getMember();
    if (invoker == null) reply("Du existierst nicht");
    return invoker;
  }

  protected DiscordUser getInvoker() {
    return getInvokingMember() == null ? null : DiscordUserFactory.getDiscordUser(getInvokingMember());
  }

  protected Msg getMessage() {
    try {
      return getClass().asSubclass(this.getClass())
          .getDeclaredMethod("execute", clazz)
          .getAnnotation(Msg.class);
    } catch (NoSuchMethodException ignored) {
      reply("Internal Error");
    }
    return null;
  }

  protected boolean send(boolean condition, Object... data) {
    return condition ? sendMessage(data) : errorMessage(data);
  }

  protected boolean sendMessage(Object... data) {
    final var annotation = getMessage();
    return annotation != null && performMsg(false, annotation, data);
  }

  protected boolean errorMessage(Object... data) {
    final var annotation = getMessage();
    return annotation != null && performMsg(true, annotation, data);
  }

  private boolean performMsg(boolean error, Msg annotation, Object[] data) {
    final String output = error ? annotation.error() : annotation.value();
    if (customEmbedData.isEmpty() && annotation.description().equals("keine Daten")) return reply(output.format(data), annotation.ephemeral());

    final List<EmbedWrapper> wrappers = new ArrayList<>();
    final var builder = new InfoPanelBuilder(output, annotation.description(), customEmbedData, null);
    wrappers.add(builder.build());

    final List<String> wrapperStrings = new ArrayList<>();
    StringBuilder out = new StringBuilder();
    for (EmbedWrapper wrapper : wrappers) {
      for (String t : wrapper.merge()) {
        if (out.length() + t.length() > Const.DISCORD_MESSAGE_MAX_CHARACTERS) {
          wrapperStrings.add(out.toString());
          out = new StringBuilder(t);
        } else {
          out.append(t);
        }
        out.append("\n\n\n\n");
      }
    }

    out.append("zuletzt aktualisiert ").append(TimeFormat.AUTO.now());
    wrapperStrings.add(out.toString());

    final List<MessageEmbed> wrapperEmbeds = wrappers.stream().flatMap(wrapper -> wrapper.getEmbeds().stream()).toList();

    final WebhookMessageCreateAction<?> message;
    if (!wrapperStrings.isEmpty()) {
      message = event.getHook().sendMessage(wrapperStrings.get(0)).addEmbeds(wrapperEmbeds);

      if (wrapperStrings.size() > 1 && !annotation.ephemeral()) {
        IntStream.range(1, wrapperStrings.size()).forEach(i ->
            ((SlashCommandInteractionEvent) event).getChannel().sendMessage(wrapperStrings.get(i)).queue());
      }

    } else if (!wrapperEmbeds.isEmpty()) message = event.getHook().sendMessageEmbeds(wrapperEmbeds);
    else message = event.getHook().sendMessage("no Data");

    message.setEphemeral(annotation.ephemeral()).queue();
    end = true;
    customEmbedData.clear();
    return true;
  }

  protected boolean sendModal() {
    return sendModal(false, 0);
  }

  protected boolean sendModal(int index) {
    return sendModal(false, index);
  }

  protected boolean sendModal(boolean someBool) {
    return sendModal(someBool, 0);
  }

  protected boolean sendModal(boolean someBool, int index) {
    if (event == null) return false;

    final UseView view = getModalView();
    if (view == null) return false;

    final String modalId = view.value()[index];
    final Modal modal = new InputHandler.Find(getInvoker(), getTarget(), event).getModal(modalId, someBool);
    if (modal == null) return false;
    if (!(event instanceof GenericCommandInteractionEvent)) return false;

    ((GenericCommandInteractionEvent) event).replyModal(modal).queue();
    end = true;
    return true;
  }

  private UseView getModalView() {
    try {
      return getClass().asSubclass(this.getClass())
          .getDeclaredMethod("execute", clazz)
          .getAnnotation(UseView.class);
    } catch (NoSuchMethodException ignored) {  }
    return null;
  }

  protected Member getTargetMember() {
    if (event == null) {
      reply("Internal Error");

    } else if (event instanceof UserContextInteractionEvent context) {
      final Member invoker = context.getTargetMember();
      if (invoker == null) reply("Du existierst nicht");
      return invoker;

    } else if (event instanceof SlashCommandInteractionEvent) {
      return getInvokingMember();
    }
    return null;
  }

  protected DiscordUser getTarget() {
    return getTargetMember() == null ? null : DiscordUserFactory.getDiscordUser(getTargetMember());
  }

  protected boolean hasNoModal() {
    try {
      final UseView execute = getClass().asSubclass(this.getClass())
          .getDeclaredMethod("execute", clazz)
          .getAnnotation(UseView.class);
      return execute == null;
    } catch (NoSuchMethodException exception) {
      throw new RuntimeException(exception);
    }
  }
}
