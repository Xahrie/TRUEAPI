package de.xahrie.trues.api.community.orgateam;

import java.util.List;

import de.xahrie.trues.api.community.orgateam.teamchannel.TeamChannel;
import de.xahrie.trues.api.community.orgateam.teamchannel.TeamChannelRepository;
import de.xahrie.trues.api.community.orgateam.teamchannel.TeamChannelType;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.discord.channel.ChannelType;
import de.xahrie.trues.api.discord.channel.DiscordChannelType;
import de.xahrie.trues.api.discord.util.Jinx;
import de.xahrie.trues.api.util.StringUtils;
import de.xahrie.trues.api.util.io.log.DevInfo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import lombok.extern.java.Log;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.jetbrains.annotations.Nullable;

@ExtensionMethod(StringUtils.class)
@Log
@RequiredArgsConstructor
public class OrgaTeamChannelHandler {
  private final OrgaTeam team;

  @Nullable
  public TeamChannel get(TeamChannelType teamChannelType, boolean create) {
    final TeamChannel channel = getExistingChannelOf(teamChannelType);
    return channel == null ? (create ? createChannel(teamChannelType) : null) : channel;
  }

  /**
   * Erhalte {@link TeamChannel} nach {@link TeamChannelType} <br>
   * Ist der Channel nicht vorhanden wird er erstellt
   */
  @Nullable
  public TeamChannel get(TeamChannelType teamChannelType) {
    return get(teamChannelType, true);
  }

  void updateChannels() {
    final String categoryName = getCategoryName();
    final TeamChannel teamChannel = get(TeamChannelType.CATEGORY);
    if (teamChannel == null) {
      final RuntimeException e = new NullPointerException("Team Channel sollte bereits erstellt sein!");
      new DevInfo().severe(e);
      throw new RuntimeException(e);
    }

    teamChannel.setName(categoryName);
    teamChannel.getChannel().getManager().setName(categoryName).queue();
  }

  private TeamChannel getExistingChannelOf(@NonNull TeamChannelType teamChannelType) {
    return new Query<>(TeamChannel.class).where("orga_team", team).and("teamchannel_type", teamChannelType).entity();
  }

  String getCategoryName() {
    return team.getName() + " (" + team.getAbbreviation() + ")";
  }

  /**
   * Erstelle erforderliche Channel für ein Team
   */
  private TeamChannel createChannel(TeamChannelType channelType) {
    final TeamChannel category = getExistingChannelOf(TeamChannelType.CATEGORY);
    if (category == null) createChannels();
    else {
      switch (channelType) {
        case SCOUTING -> DiscordChannelType.TEXT.createTeamChannel((Category) category.getChannel(), TeamChannelType.SCOUTING, team);
        case INFO -> DiscordChannelType.TEXT.createTeamChannel((Category) category.getChannel(), TeamChannelType.INFO, team);
        case CHAT -> DiscordChannelType.TEXT.createTeamChannel((Category) category.getChannel(), TeamChannelType.CHAT, team);
        case VOICE, PRACTICE -> DiscordChannelType.VOICE.createTeamChannel((Category) category.getChannel(), TeamChannelType.PRACTICE, team);
        case CATEGORY -> createChannels();
      }
    }
    return getExistingChannelOf(channelType);
  }

  void createChannels() {
    Jinx.instance.getGuild().createCategory(team.getChannels().getCategoryName()).queue(category -> {
      DiscordChannelType.TEXT.createTeamChannel(category, TeamChannelType.CHAT, team);
      DiscordChannelType.TEXT.createTeamChannel(category, TeamChannelType.INFO, team);
      DiscordChannelType.TEXT.createTeamChannel(category, TeamChannelType.SCOUTING, team);
      DiscordChannelType.STAGE.createTeamChannel(category, TeamChannelType.PRACTICE, team);
    });
  }

  public List<TeamChannel> getChannels() {
    return new Query<>(TeamChannel.class).where("orga_team", team).entityList();
  }

  /**
   * Erstelle einen Teamchannel automatisch
   */
  @NonNull
  public static TeamChannel createTeamChannelEntity(@NonNull GuildChannel channel, @NonNull OrgaTeam team) {
    final TeamChannelType channelType = TeamChannelType.fromChannel(channel);
    final DiscordChannelType discordChannelType = DiscordChannelType.valueOf(channel.getType().name());
    final ChannelType type = channelType.equals(TeamChannelType.VOICE) ? ChannelType.ORGA_INTERN : ChannelType.TEAM;
    return new TeamChannel(channel.getIdLong(), channel.getName(), type, discordChannelType, team, channelType).forceCreate();
  }

  public void updateLeaderPermissions() {
    getChannels().forEach(TeamChannel::updateLeaderPermissions);
  }
}
