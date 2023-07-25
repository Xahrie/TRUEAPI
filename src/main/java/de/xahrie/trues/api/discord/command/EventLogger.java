package de.xahrie.trues.api.discord.command;

import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.discord.user.DiscordUserFactory;
import de.xahrie.trues.api.discord.util.Jinx;
import de.xahrie.trues.api.logging.ServerLog;
import de.xahrie.trues.api.util.StringUtils;
import lombok.experimental.ExtensionMethod;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.channel.GenericChannelEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.override.GenericPermissionOverrideEvent;
import net.dv8tion.jda.api.events.guild.update.GenericGuildUpdateEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.role.GenericRoleEvent;
import net.dv8tion.jda.api.events.stage.GenericStageInstanceEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import org.jetbrains.annotations.NotNull;

@ExtensionMethod(StringUtils.class)
public class EventLogger extends ListenerAdapter {
  @Override
  public void onGenericEvent(@NotNull GenericEvent event) {
    if (event instanceof MessageDeleteEvent ||
        event instanceof MessageUpdateEvent ||
        event instanceof GuildMemberJoinEvent ||
        event instanceof GuildMemberRemoveEvent ||
        event instanceof GuildBanEvent ||
        event instanceof GuildUnbanEvent ||
        event instanceof GenericPermissionOverrideEvent ||
        event instanceof GenericStageInstanceEvent ||
        event instanceof GenericGuildUpdateEvent ||
        event instanceof GenericChannelEvent ||
        event instanceof GenericRoleEvent ||
        event instanceof GenericInteractionCreateEvent) {
      final Member guildMember = determineGuildMember(event);
      if (guildMember != null && (guildMember.getUser().equals(
              Jinx.instance.getClient().getSelfUser()) || guildMember.getUser().isBot())) return;
      if (event instanceof MessageUpdateEvent me && me.getAuthor().isBot()) return;
      final DiscordUser discordUser = guildMember == null ? null : DiscordUserFactory.getDiscordUser(guildMember);
      final String details = determineDetails(event);
      new ServerLog(discordUser, details, ServerLog.ServerLogAction.fromClass(event.getClass())).forceCreate();
    }
  }

  private String determineDetails(GenericEvent event) {
    if (event instanceof CommandInteraction guildBanEvent) return guildBanEvent.getName() + " " + guildBanEvent.getCommandString();
    if (event instanceof MessageUpdateEvent messageUpdateEvent) return messageUpdateEvent.getMessage().getContentDisplay().keep(1000);
    return event.getClass().getSimpleName();
  }

  private Member determineGuildMember(GenericEvent event) {
    if (event instanceof GuildMemberJoinEvent memberJoinEvent) return memberJoinEvent.getMember();
    if (event instanceof GuildMemberRemoveEvent memberRemoveEvent) return memberRemoveEvent.getMember();
    if (event instanceof GuildBanEvent guildBanEvent) return Jinx.instance.getMember(guildBanEvent.getUser());
    if (event instanceof GuildUnbanEvent guildBanEvent) return Jinx.instance.getMember(guildBanEvent.getUser());
    if (event instanceof GenericInteractionCreateEvent createEvent) return Jinx.instance.getMember(createEvent.getUser());
    return null;
  }
}
