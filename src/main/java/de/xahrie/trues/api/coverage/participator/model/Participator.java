package de.xahrie.trues.api.coverage.participator.model;

import java.io.Serial;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import de.xahrie.trues.api.coverage.league.model.AbstractLeague;
import de.xahrie.trues.api.coverage.match.model.Match;
import de.xahrie.trues.api.coverage.participator.TeamLineup;
import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import de.xahrie.trues.api.coverage.team.model.Team;
import de.xahrie.trues.api.database.connector.SQLUtils;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.database.query.SQLEnum;
import de.xahrie.trues.api.scouting.ScoutingGameType;
import de.xahrie.trues.api.util.Util;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Team als Teilnehmer an einem Match
 */
@Getter
@Table("coverage_team")
@ExtensionMethod(SQLUtils.class)
public class Participator implements Entity<Participator>, Comparable<Participator> {
  @Serial
  private static final long serialVersionUID = -547972848562058467L;

  public static Participator ADMIN(Match match) {
    return new Participator(match, false, new Team("Administration", "Admin"));
  }

  @Setter
  private int id; // coverage_team_id
  private final int matchId; // coverage
  private final boolean home; // first
  private final ParticipatorRoute route;
  private Integer teamId; // team
  private short wins = 0; // wins
  private Long discordEventId; // discord_event
  private Long messageId; // discord_message
  private Integer mmr; // lineup_mmr

  public Integer getMmr() {
    return mmr == null ? Util.avoidNull(getTeam(), 0, AbstractTeam::getLastMMR) : mmr;
  }

  private AbstractTeam team;

  @Nullable
  public AbstractTeam getTeam() {
    if (team == null) this.team = new Query<>(AbstractTeam.class).entity(teamId);
    return team;
  }

  private Match match;

  public Match getMatch() {
    if (match == null) this.match = new Query<>(Match.class).entity(matchId);
    return match;
  }

  public Participator(Match match, boolean home) {
    this(match, home, (ParticipatorRoute) null);
  }

  public Participator(Match match, boolean home, ParticipatorRoute route) {
    this.match = match;
    this.matchId = match == null ? 0 : match.getId();
    this.home = home;
    this.route = route;
  }

  public Participator(Match match, boolean home, @NonNull AbstractTeam team) {
    this.match = match;
    this.matchId = match == null ? 0 : match.getId();
    this.home = home;
    this.route = null;
    this.team = team;
    this.teamId = team.getId();
  }

  private Participator(int id, int matchId, boolean home, Integer teamId, short wins, ParticipatorRoute route, Long discordEventId, Long messageId, Integer mmr) {
    this.id = id;
    this.matchId = matchId;
    this.home = home;
    this.teamId = teamId;
    this.wins = wins;
    this.route = route;
    this.discordEventId = discordEventId;
    this.messageId = messageId;
    this.mmr = mmr;
  }

  public static Participator get(List<Object> objects) {
    return new Participator(
        (int) objects.get(0),
        (int) objects.get(1),
        (boolean) objects.get(2),
        (Integer) objects.get(6),
        objects.get(7).shortValue(),
        new ParticipatorRoute(new Query<>(AbstractLeague.class).entity(objects.get(3)), new SQLEnum<>(ParticipatorRoute.RouteType.class).of(objects.get(4)), objects.get(5).shortValue()),
        (Long) objects.get(8),
        (Long) objects.get(9),
        (Integer) objects.get(10)
    );
  }

  private TeamLineup teamLineupHandler;

  @NonNull
  public TeamLineup getTeamLineup() {
    return getTeamLineup(ScoutingGameType.TEAM_GAMES, 180);
  }

  @NonNull
  public TeamLineup getTeamLineup(ScoutingGameType gameType, int days) {
    if (gameType.equals(ScoutingGameType.TEAM_GAMES) && days == 180) {
      if (teamLineupHandler == null) this.teamLineupHandler = new TeamLineup(this, gameType, days);
      return teamLineupHandler;
    }
    return new TeamLineup(this, gameType, days);
  }

  public void setDiscordEventId(Long discordEventId) {
    if (!Objects.equals(this.discordEventId, discordEventId)) {
      new Query<>(Participator.class).col("discord_event", discordEventId).update(id);
    }
    this.discordEventId = discordEventId;
  }

  @Override
  public Participator create() {
    final AbstractLeague league = route == null ? null : route.getLeague();
    final ParticipatorRoute.RouteType routeType = route == null ? null : route.getType();
    final Short routeValue = route == null ? null : route.getValue();
    return new Query<>(Participator.class).key("coverage", matchId).key("first", home)
        .col("route_group", league).col("route_type", routeType).col("route_value", routeValue)
        .col("team", teamId).col("wins", wins).col("discord_event", discordEventId).col("discord_message", messageId).col("lineup_mmr", mmr)
        .insert(this);
  }

  @Override
  public void delete() {
    setTeam(null);
    setWins((short) 0);
  }

  public void setMessageId(Long messageId) {
    this.messageId = messageId;
    new Query<>(Participator.class).col("discord_message", messageId).update(id);
  }

  public void setTeam(@Nullable AbstractTeam team) {
    if (getMatch().checkAddParticipatingTeam(this, team)) {
      new Query<>(Lineup.class).where("coverage_team", this).delete(List.of());
      this.team = team;
      this.teamId = Util.avoidNull(team, AbstractTeam::getId);
      new Query<>(Participator.class).col("team", team).update(id);
      getMatch().updateResult();
    }
  }

  public void setWins(int wins) {
    if (this.wins != wins) {
      this.wins = (short) wins;
      new Query<>(Participator.class).col("wins", wins).update(id);
    }
  }

  public void setMmr(Integer mmr) {
    if (!Objects.equals(this.mmr, mmr)) {
      this.mmr = mmr;
      new Query<>(Participator.class).col("lineup_mmr", wins).update(id);
    }
  }

  public String getAbbreviation() {
    return Util.avoidNull(getTeam(), Util.avoidNull(route, "TBD", ParticipatorRoute::toString), AbstractTeam::getAbbreviation);
  }

  public String getName() {
    if (teamId == null) return route == null ? "TBD" : route.toString();
    return Objects.requireNonNull(getTeam()).getName();
  }

  @Override
  public int compareTo(@NotNull Participator o) {
    return Comparator.comparing(Participator::getMatch).thenComparing(Participator::isHome).compare(this, o);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof final Participator that)) return false;
    if (id == that.getId()) return true;
    return Objects.equals(getMatchId(), that.getMatchId()) && Objects.equals(getTeamId(), that.getTeamId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getMatchId(), getTeamId());
  }

  private ScheduledEventHandler event;

  public ScheduledEventHandler getEvent() {
    if (event == null) this.event = new ScheduledEventHandler(this);
    return event;
  }
}
