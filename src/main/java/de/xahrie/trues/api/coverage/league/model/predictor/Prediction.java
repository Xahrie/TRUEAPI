package de.xahrie.trues.api.coverage.league.model.predictor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.xahrie.trues.api.coverage.league.model.AbstractLeague;
import de.xahrie.trues.api.coverage.match.MatchResult;
import de.xahrie.trues.api.coverage.team.leagueteam.LeagueTeam;
import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public record Prediction(Map<AbstractTeam, Integer> teamPoints) {
  public static Prediction generate(AbstractLeague league) {
    return new Prediction(
        league.getLeagueTeams().stream().collect(Collectors.toMap(LeagueTeam::getTeam, leagueTeam -> leagueTeam.getScore().getStanding().wins()))
    );
  }

  public Prediction add(List<MatchResult> outcomes) {
    outcomes.forEach(outcome -> add(outcome.getMatch().getHome().getTeam(), outcome.getHomeScore()));
    return this;
  }

  public Prediction add(AbstractTeam team, Integer wins) {
    teamPoints.merge(team, wins, Integer::sum);
    return this;
  }

  @Nullable
  @Contract(pure = true)
  public LeagueResult getResultOfTeam(AbstractTeam team) {
    final Integer points = teamPoints.get(team);
    if (points == null) return null;

    final int place = (int) teamPoints.values().stream().filter(i -> i > points).count() + 1;
    final int with = (int) teamPoints.values().stream().filter(i -> i.equals(points)).count();
    final LeagueResult.Type type = LeagueResult.getType(place, with);
    return new LeagueResult(place, points, type);
  }
}
