package de.xahrie.trues.api.discord.channel;

import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.community.orgateam.OrgaTeamChannelHandler;
import de.xahrie.trues.api.community.orgateam.OrgaTeamFactory;
import de.xahrie.trues.api.community.orgateam.teamchannel.TeamChannel;
import de.xahrie.trues.api.community.orgateam.teamchannel.TeamChannelRepository;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.util.StringUtils;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import lombok.extern.java.Log;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

@Log
@ExtensionMethod(StringUtils.class)
public class DiscordChannelFactory {
  /**
   * Erhalte {@link DiscordChannel} vom GuildChannel <br>
   * Wenn noch nicht vorhanden erstelle Datenbankeintrag
   */
  @NonNull
  public static AbstractDiscordChannel getDiscordChannel(@NonNull GuildChannel channel) {
    final AbstractDiscordChannel discordChannel = new Query<>(AbstractDiscordChannel.class).where("discord_id", channel.getIdLong()).entity();
    return discordChannel != null ? discordChannel : createChannel(channel);
  }

  /**
   * Erstelle Channeleintrag in Datenbank, sofern noch nicht vorhanden
   */
  @NonNull
  private static AbstractDiscordChannel createChannel(@NonNull GuildChannel channel) {
    OrgaTeam orgaTeam = OrgaTeamFactory.getTeamFromChannel(channel);
    if (orgaTeam == null && channel.getName().contains(" (")) {
      final String categoryAbbr = channel.getName().between(" (", ")");
      orgaTeam = new Query<>(OrgaTeam.class).where("team_abbr_created", categoryAbbr).entity();
    }

    return orgaTeam != null ? OrgaTeamChannelHandler.createTeamChannelEntity(channel, orgaTeam) :
        new DiscordChannel(channel.getIdLong(), channel.getName(), ChannelType.fromChannel(channel), DiscordChannelType.valueOf(channel.getType().name())).forceCreate();
  }

  public static void removeTeamChannel(@NonNull GuildChannel channel) {
    final TeamChannel teamChannel = TeamChannelRepository.getTeamChannelFromChannel(channel);
    if (teamChannel != null) teamChannel.forceDelete();
  }
}
