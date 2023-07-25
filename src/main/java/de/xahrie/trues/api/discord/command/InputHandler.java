package de.xahrie.trues.api.discord.command;

import de.xahrie.trues.api.discord.builder.modal.ModalImpl;
import de.xahrie.trues.api.discord.command.context.ContextCommand;
import de.xahrie.trues.api.discord.command.slash.SlashCommand;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.discord.util.Jinx;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;

public class InputHandler extends ListenerAdapter {

  @Override
  public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
    for (SlashCommand slashCommand : Jinx.instance.getCommands()) {
      if (slashCommand.getName().equals(event.getName())) {
        slashCommand.handleCommand(event);
        return;
      }
    }
    event.reply("Dieser Command wurde nicht gefunden!").queue();
  }

  @Override
  public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
    Jinx.instance.getCommands().stream().filter(slashCommand -> slashCommand.getName().equals(event.getName())).findFirst()
        .ifPresent(slashCommand -> slashCommand.handleAutoCompletion(event));
  }

  public void onUserContextInteraction(@NotNull UserContextInteractionEvent event) {
    for (ContextCommand contextCommand : Jinx.instance.getContext()) {
      if (contextCommand.getName().equals(event.getName())) {
        contextCommand.handleCommand(event);
        return;
      }
    }
    event.reply("Dieser Command wurde nicht gefunden!").queue();
  }

  @Override
  public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {
    super.onMessageContextInteraction(event);
  }

  @Override
  public void onModalInteraction(@NotNull ModalInteractionEvent event) {
    for (ModalImpl modal : Jinx.instance.getModals()) {
      if (modal.getName().equals(event.getModalId())) {
        modal.setEvent(event);
        event.deferReply(true).queue();
        modal.execute(event);
        break;
      }
    }
  }

  public record Find(DiscordUser invoker, DiscordUser target, IReplyCallback event) {
    public Modal getModal(String type, boolean someBool) {
      final ModalImpl base = Jinx.instance.getModals().stream().filter(modalBase -> modalBase.getName().equals(type)).findFirst().orElse(null);
      if (base == null) return null;

      base.setTarget(target);
      return base.getModal(someBool);
    }
  }
}
