package de.xahrie.trues.api.coverage.participator.model;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import de.xahrie.trues.api.community.orgateam.teamchannel.TeamChannelType;
import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.community.orgateam.teamchannel.TeamChannel;
import de.xahrie.trues.api.coverage.match.log.MatchLogBuilder;
import de.xahrie.trues.api.coverage.match.model.LeagueMatch;
import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import de.xahrie.trues.api.discord.util.Jinx;
import de.xahrie.trues.api.util.StringUtils;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ScheduledEventHandler(Participator participator) {

  @Nullable
  public ScheduledEvent getEvent() {
    if (participator.getDiscordEventId() == null) return null;
    return Jinx.instance.getGuild().getScheduledEventById(participator.getDiscordEventId());
  }
  public void updateScheduledEvent() {
    if (participator.getTeamId() == null) return;
    if (participator.getMatch().getStart().isBefore(LocalDateTime.now())) return;

    final AbstractTeam team = participator.getTeam();
    assert team != null;
    final OrgaTeam orgaTeam = team.getOrgaTeam();
    if (orgaTeam == null) return;

    final TeamChannel teamChannel = orgaTeam.getChannels().get(TeamChannelType.PRACTICE);
    if (teamChannel == null) return;

    final ScheduledEvent event = getEvent();
    if (event == null) {
      createScheduledEvent();
      return;
    }

    event.getManager().setName(title()).setLocation(practiceChannel()).setStartTime(start())
        .setDescription(description())
        .setEndTime(end())
        .queue();
  }

  private void createScheduledEvent() {
    Jinx.instance.getGuild().createScheduledEvent(title(), practiceChannel(), start())
        .setDescription(description())
        .setEndTime(end())
        .queue(scheduledEvent -> participator.setDiscordEventId(scheduledEvent.getIdLong()));
  }

  @NotNull
  private String description() {
    String description = "";
    if (participator.getMatch() instanceof LeagueMatch leagueMatch)
      description += "https://www.primeleague.gg/leagues/matches/" + leagueMatch.getMatchId() + "\n";

    description += new MatchLogBuilder(participator.getMatch(), participator.getTeam()).toString();
    return StringUtils.keep(description, 1000);
  }

  @NotNull
  @Contract(" -> new")
  private OffsetDateTime start() {
    return participator.getMatch().getStart().atZone(ZoneId.systemDefault()).toOffsetDateTime();
  }

  @NotNull
  @Contract(" -> new")
  private OffsetDateTime end() {
    return participator.getMatch().getExpectedTimeRange().getEndTime().atZone(ZoneId.systemDefault()).toOffsetDateTime();
  }

  @NotNull
  private String title() {
    final String title = participator.getMatch().toString();
    return StringUtils.keep(title, 100);
  }

  private GuildChannel practiceChannel() {
    final AbstractTeam team = participator.getTeam();
    assert team != null;
    final TeamChannel teamChannel = team.getOrgaTeam().getChannels().get(TeamChannelType.PRACTICE);
    assert teamChannel != null;
    return teamChannel.getChannel();
  }
}
