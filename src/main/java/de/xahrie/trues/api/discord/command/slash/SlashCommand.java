package de.xahrie.trues.api.discord.command.slash;

import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.community.orgateam.OrgaTeamFactory;
import de.xahrie.trues.api.discord.command.PermissionCheck;
import de.xahrie.trues.api.discord.command.slash.annotations.Command;
import de.xahrie.trues.api.discord.command.slash.annotations.Option;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.discord.user.DiscordUserFactory;
import de.xahrie.trues.api.discord.util.Jinx;
import de.xahrie.trues.api.discord.util.Replyer;
import de.xahrie.trues.api.util.StringUtils;
import de.xahrie.trues.api.util.Util;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import lombok.extern.java.Log;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ExtensionMethod(StringUtils.class)
@Log
public abstract class SlashCommand extends Replyer {
  private String description;
  private PermissionCheck permissionCheck;
  private List<OptionData> options = new ArrayList<>();
  private List<SlashCommand> subCommands = List.of();
  private List<AutoCompletion> completions = new ArrayList<>();
  private boolean defered = false;

  public Find find(String key) {
    return new Find(key);
  }

  public SlashCommand() {
    super(SlashCommandInteractionEvent.class);
    final Command annotation = getClass().asSubclass(this.getClass()).getAnnotation(Command.class);
    if (annotation == null) return;

    this.name = annotation.name();
    this.description = annotation.descripion();
    this.permissionCheck = new PermissionCheck(annotation.perm());

    for (Option option : annotation.options()) {
      final boolean hasCompletion = !option.completion().isEmpty();
      final var optionData = new OptionData(option.type(), option.name(), option.description(), option.required(), hasCompletion);

      Arrays.stream(option.choices()).forEach(choice -> optionData.addChoice(choice, choice));
      this.options.add(optionData);
      if (hasCompletion) {
        final var completion = new AutoCompletion(option.name(), option.completion());
        this.completions.add(completion);
      }
    }
  }

  protected SlashCommand(SlashCommand... commands) {
    this();
    this.subCommands = List.of(commands);
  }

  public abstract boolean execute(SlashCommandInteractionEvent event);

  public void handleAutoCompletion(CommandAutoCompleteInteractionEvent event) {
    completions.stream().filter(completion -> completion.optionName().equals(event.getFocusedOption().getName()))
        .findFirst().ifPresent(completion -> event.replyChoices(completion.getData().subList(0, Math.min(25, completion.getData().size()))).queue());
  }

  public void handleCommand(SlashCommandInteractionEvent event) {
    handleCommand(event.getFullCommandName(), event);
  }

  private void handleCommand(String fullCommandName, SlashCommandInteractionEvent event) {
    if (fullCommandName.startsWith(name)) {
      if (!defered && hasNoModal()) {
        final var annotation = getMessage();
        event.deferReply(annotation == null || annotation.ephemeral()).queue();
        defered = true;
      }

      if (!permissionCheck.check(event.getMember())) {
        reply("Dir fehlen die nötigen Rechte.");
      } else {
        this.event = event;
        this.execute(event);
      }
    }

    if (defered) this.defered = false;

    if (fullCommandName.split(" ").length == 1) {
      return;
    }

    subCommands.stream().filter(subCommand -> subCommand.getName().equals(fullCommandName.split(" ")[1])).findFirst()
        .ifPresent(validSubCommand -> validSubCommand.handleCommand(fullCommandName.substring(fullCommandName.indexOf(" ")), event));

    if (!end) reply("Internal Error");
  }

  public SlashCommandData commandData() {
    final var subCmds = subCommands.stream().filter(subCommand -> subCommand.getSubCommands().isEmpty()).toList();
    final var subCmdGroups = subCommands.stream().filter(subCommand -> !subCommand.getSubCommands().isEmpty()).toList();
    for (var option : options) {
      final boolean hasAutoComplete = completions.stream().anyMatch(autoCompletion -> autoCompletion.optionName().equals(option.getName()));
      option.setAutoComplete(hasAutoComplete);
    }
    log.info("Registriere " + name);
    return Commands.slash(name, description)
        .setGuildOnly(true)
        .addOptions(options)
        //.setDefaultPermissions(DefaultMemberPermissions.DISABLED)
        .addSubcommands(subCmds.stream().map(SlashCommand::getSubCommand).toList())
        .addSubcommandGroups(subCmdGroups.stream().map(SlashCommand::getSubCommandGroup).toList());
  }

  private SubcommandData getSubCommand() {
    final String commandName = name.contains(" ") ? name.after(" ", -1) : name;
    return new SubcommandData(commandName, description).addOptions(this.options);
  }

  private SubcommandGroupData getSubCommandGroup() {
    final String commandName = name.contains(" ") ? name.after(" ", -1) : name;
    return new SubcommandGroupData(commandName, description).addSubcommands(subCommands.stream().map(SlashCommand::getSubCommand).toList());
  }

  public class Find {
    private final String key;

    public Find(String key) {
      this.key = key;
    }

    public Boolean bool() {
      return bool(null);
    }

    public Boolean bool(Boolean defaultValue) {
      final OptionMapping option = ((SlashCommandInteractionEvent) event).getOption(key);
      return option == null ? defaultValue : option.getAsBoolean();
    }

    public GuildChannelUnion channel() {
      return channel(null);
    }

    public GuildChannelUnion channel(GuildChannelUnion defaultValue) {
      final OptionMapping option = ((SlashCommandInteractionEvent) event).getOption(key);
      return option == null ? defaultValue : option.getAsChannel();
    }

    public String string() {
      return string(null);
    }

    public String string(String defaultValue) {
      final OptionMapping option = ((SlashCommandInteractionEvent) event).getOption(key);
      return option == null ? defaultValue : option.getAsString();
    }

    public Integer integer() {
      return integer(null);
    }

    public Integer integer(Integer defaultValue) {
      final OptionMapping option = ((SlashCommandInteractionEvent) event).getOption(key);
      if (option == null) return defaultValue;
      try {
        return option.getAsInt();
      } catch (NumberFormatException ignored) {
        return null;
      }
    }

    public Long bigInt() {
      return bigInt(null);
    }

    public Long bigInt(Long defaultValue) {
      final OptionMapping option = ((SlashCommandInteractionEvent) event).getOption(key);
      try {
        return option == null ? defaultValue : option.getAsLong();
      } catch (NumberFormatException ignored) {
        return null;
      }
    }

    public DiscordUser discordUser() {
      return Util.avoidNull(member(), DiscordUserFactory::getDiscordUser);
    }

    public DiscordUser discordUser(Member defaultValue) {
      return Util.avoidNull(member(defaultValue), DiscordUserFactory::getDiscordUser);
    }

    public Member member() {
      return member(null);
    }

    public Member member(Member defaultValue) {
      final OptionMapping option = ((SlashCommandInteractionEvent) event).getOption(key);
      return option == null ? defaultValue : option.getAsMember();
    }

    public LocalDateTime time() {
      return time(null);
    }

    public LocalDateTime time(LocalDateTime defaultValue) {
      final OptionMapping option = ((SlashCommandInteractionEvent) event).getOption(key);
      return option == null ? defaultValue : option.getAsString().getDateTime();
    }

    public <T extends Enum<T>> T toEnum(Class<T> clazz) {
      return toEnum(clazz, null);
    }

    public <T extends Enum<T>> T toEnum(Class<T> clazz, T defaultValue) {
      return string().toEnum(clazz, defaultValue);
    }
  }

  public GuildChannel getChannel() {
    return Jinx.instance.getChannels().getChannel(Util.nonNull(event.getChannel()).getIdLong());
  }

  @Nullable
  public OrgaTeam getLocatedTeam() {
    return OrgaTeamFactory.getTeamFromChannel(getChannel());
  }
}
