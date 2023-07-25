package de.xahrie.trues.api.logging;


import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.discord.util.Jinx;
import de.xahrie.trues.api.util.Const;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class TeamLogFactory {
  private static final TextChannel LOG_CHANNEL = Jinx.instance.getClient().getTextChannelById(Const.Channels.TEAM_LOGGING_CHANNEL);

  public static void create(DiscordUser invoker, DiscordUser target, String details, TeamLog.TeamLogAction action, OrgaTeam team) {
    new TeamLog(invoker, target, details, action, team).forceCreate();
    if (LOG_CHANNEL == null) throw new NullPointerException("Team-Log Channel wurde gelöscht");
    LOG_CHANNEL.sendMessage(details).queue();
  }
}
