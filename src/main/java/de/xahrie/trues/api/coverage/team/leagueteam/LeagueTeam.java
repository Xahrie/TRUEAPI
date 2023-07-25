package de.xahrie.trues.api.coverage.team.leagueteam;

import java.io.Serial;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.xahrie.trues.api.coverage.league.model.AbstractLeague;
import de.xahrie.trues.api.coverage.league.model.LeagueTier;
import de.xahrie.trues.api.coverage.match.model.LeagueMatch;
import de.xahrie.trues.api.coverage.participator.model.Participator;
import de.xahrie.trues.api.coverage.team.model.TeamScore;
import de.xahrie.trues.api.coverage.match.MatchResult;
import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import de.xahrie.trues.api.database.connector.SQLUtils;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;

@Getter
@Table("league_team")
@ExtensionMethod(SQLUtils.class)
public class LeagueTeam implements Entity<LeagueTeam>, Comparable<LeagueTeam> {
  @Serial
  private static final long serialVersionUID = -2748540818479532130L;
  @Setter
  private int id; // league_team_id
  private final int leagueId; // league
  private final int teamId; // team
  private final TeamScore score; // current_place, current_wins, current_losses

  public LeagueTeam(@NotNull
  AbstractLeague league, @NotNull AbstractTeam team, @NotNull TeamScore score) {
    this.league = league;
    this.leagueId = league.getId();
    this.team = team;
    this.teamId = team.getId();
    this.score = score;
  }

  private LeagueTeam(int id, int leagueId, int teamId, TeamScore score) {
    this.id = id;
    this.leagueId = leagueId;
    this.teamId = teamId;
    this.score = score;
  }

  public static LeagueTeam get(List<Object> objects) {
    return new LeagueTeam(
        (int) objects.get(0),
        (int) objects.get(1),
        (int) objects.get(2),
        new TeamScore(objects.get(3).shortValue(), objects.get(4).shortValue(), objects.get(5).shortValue())
    );
  }

  @Override
  public LeagueTeam create() {
    return new Query<>(LeagueTeam.class).key("league", leagueId).key("team", teamId)
        .col("current_place", score.place()).col("current_wins", score.wins()).col("current_losses", score.losses()).insert(this, leagueTeam -> getLeague().getLeagueTeams().add(this));
  }

  @Override
  public int compareTo(@NotNull LeagueTeam o) {
    return Comparator.comparing(LeagueTeam::getLeague)
        .thenComparing(LeagueTeam::getScore).compare(this, o);
  }

  @Override
  public String toString() {
    return getLeague().getName() + " - " + score.toString();
  }

  public TeamScore getExpectedScore() {
    final Map<AbstractTeam, MatchResult> results = new TreeMap<>();
    for (final LeagueMatch match : getLeague().getMatches()) {
      for (final Participator participator : match.getParticipators()) {
        final AbstractTeam participatingTeam = participator.getTeam();
        if (participatingTeam == null) continue;

        final MatchResult resultHandler = results.containsKey(participatingTeam) ? results.get(participatingTeam) :
            new MatchResult(match, 0, 0);
        final MatchResult resultHandler2 = match.getResult().ofTeam(participatingTeam);
        if (resultHandler2 != null) results.put(participatingTeam, resultHandler.add(resultHandler2));
      }
    }
    final MatchResult resultHandler = results.get(getTeam());
    return new TeamScore((short) results.keySet().stream().toList().indexOf(getTeam()), (short) resultHandler.getHomeScore(), (short) resultHandler.getGuestScore());
  }

  public LeagueTier getCurrentTier() {
    return getLeague().getTier();
  }

  public LeagueTier getNext() {
    if (score.place() == 0) return LeagueTier.Swiss_Starter;
    if (getLeague().getTier().equals(LeagueTier.Swiss_Starter)) {
      if (score.wins() < 6) return LeagueTier.Division_8;
      if (score.wins() < 9) return LeagueTier.Division_7;
      if (score.wins() < 11) return LeagueTier.Division_6;
      if (score.wins() < 13) return LeagueTier.Division_5;
      return LeagueTier.Division_4_Playoffs;
    }
    if (score.place() > 6) return LeagueTier.fromIndex(getCurrentTier().getIndex() + 1);
    if (score.place() < 3) return LeagueTier.fromIndex(getCurrentTier().getIndex() - 1);
    return getCurrentTier();
  }

  private AbstractLeague league;

  public AbstractLeague getLeague() {
    if (league == null) this.league = new Query<>(AbstractLeague.class).entity(leagueId);
    return league;
  }

  private AbstractTeam team;

  public AbstractTeam getTeam() {
    if (team == null) this.team = new Query<>(AbstractTeam.class).entity(teamId);
    return team;
  }
}
