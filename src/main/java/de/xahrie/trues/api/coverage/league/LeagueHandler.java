package de.xahrie.trues.api.coverage.league;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import de.xahrie.trues.api.coverage.league.model.PRMLeague;
import de.xahrie.trues.api.coverage.match.model.Match;
import de.xahrie.trues.api.coverage.team.model.PRMTeam;
import de.xahrie.trues.api.coverage.match.MatchHandler;
import de.xahrie.trues.api.coverage.match.MatchLoader;
import de.xahrie.trues.api.coverage.team.TeamHandler;
import de.xahrie.trues.api.coverage.team.TeamLoader;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LeagueHandler extends LeagueModel implements Serializable {
  @Serial
  private static final long serialVersionUID = -5698606611490610348L;

  @Builder
  @SuppressWarnings("unused")
  LeagueHandler(String url, PRMLeague league, List<PRMTeam> teams, List<LeaguePlayday> playdays) {
    super(url, league, teams, playdays);
  }

  public void updateAll() {
    updateTeams();
    updateMatches();
    league.update();
  }

  public void updateMatches() {
    if (league.isStarter())
      return;

    playdays.stream().flatMap(playday -> playday.matches().stream())
        .filter(Match::isActive)
        .map(MatchLoader::new)
        .map(MatchLoader::load)
        .forEach(MatchHandler::update);
  }

  public void updateTeams() {
    for (PRMTeam team : teams) {
      final TeamLoader teamLoader = new TeamLoader(team);
      final TeamHandler load = teamLoader.load();
      load.update();
    }
  }
}
