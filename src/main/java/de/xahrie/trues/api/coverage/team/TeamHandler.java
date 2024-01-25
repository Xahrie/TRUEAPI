package de.xahrie.trues.api.coverage.team;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import de.xahrie.trues.api.coverage.league.LeagueLoader;
import de.xahrie.trues.api.coverage.league.model.AbstractLeague;
import de.xahrie.trues.api.coverage.league.model.PRMLeague;
import de.xahrie.trues.api.coverage.match.MatchFactory;
import de.xahrie.trues.api.coverage.match.MatchHandler;
import de.xahrie.trues.api.coverage.match.MatchLoader;
import de.xahrie.trues.api.coverage.match.model.Match;
import de.xahrie.trues.api.coverage.player.model.LoaderGameType;
import de.xahrie.trues.api.coverage.player.model.PRMPlayer;
import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.coverage.player.model.PlayerRank;
import de.xahrie.trues.api.coverage.player.model.PlayerRankHandler;
import de.xahrie.trues.api.coverage.player.model.Rank;
import de.xahrie.trues.api.coverage.season.signup.SignupFactory;
import de.xahrie.trues.api.coverage.team.leagueteam.LeagueTeam;
import de.xahrie.trues.api.coverage.team.model.PRMTeam;
import de.xahrie.trues.api.database.query.ModifyOutcome;
import de.xahrie.trues.api.util.StringUtils;
import de.xahrie.trues.api.util.Util;
import de.xahrie.trues.api.util.io.request.HTML;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;

@Getter
@ExtensionMethod(StringUtils.class)
public class TeamHandler extends TeamModel implements Serializable {
  @Serial
  private static final long serialVersionUID = 1292510240274127687L;

  @SuppressWarnings("unused")
  @Builder
  public TeamHandler(HTML html, String url, PRMTeam team, List<PRMPlayer> players) {
    super(html, url, team, players);
  }

  /**
   *
   * @param ignore wenn {@code true} lade keine Daten
   */
  public void update(boolean ignore) {
    if (TeamLoader.loadedTeams.contains(team)) return;

    final List<HTML> stages = html.findAll("section", HTML.STAGE);
    final ModifyOutcome created = updateResult(stages);
    updateRecordAndSeasons();
    if (team.getCurrentLeague() != null && ((PRMLeague) team.getCurrentLeague().getLeague()).isStarter()) handleStarterMatches(stages);

    if (!created.isNull() && !ignore) {
      System.out.println("Lade " + team.getName());
      team.getPlayers().forEach(player -> player.loadGames(LoaderGameType.CLASH_PLUS));
    }
    final double averageMMR = team.getPlayers().stream().map(Player::getRanks).map(
            PlayerRankHandler::getLastRelevant).map(PlayerRank::getRank).mapToInt(Rank::getMMR).average().orElse(0);
    team.setLastMMR((int) Math.round(averageMMR));
    team.update();

    TeamLoader.loadedTeams.add(team);
  }

  public void update() {
    update(false);
  }

  public AbstractLeague loadDivision() {
    final AbstractLeague currentLeague = Util.avoidNull(team.getCurrentLeague(), LeagueTeam::getLeague);
    if (currentLeague instanceof PRMLeague prmLeague) {
      final LeagueLoader leagueLoader = new LeagueLoader(prmLeague);
      leagueLoader.load().updateAll();
    }
    return currentLeague;
  }

  private void handleStarterMatches(@NotNull List<HTML> stages) {
    stages.getLast()
          .find("ul", HTML.MATCHES)
          .findAll("li").stream()
          .map(match -> MatchLoader.idFromURL(match.find("a").getAttribute("href")))
          .map(MatchFactory::getMatch).filter(Objects::nonNull).filter(Match::isActive)
          .map(MatchLoader::new).map(MatchLoader::load)
          .forEach(MatchHandler::update);
  }

  private void updateRecordAndSeasons() {
    List<String> teamInfos = html.find("div", HTML.TEAM_HEAD).findAll("li").stream()
        .map(HTML::text).map(str -> str.after(":")).toList();
    teamInfos = teamInfos.subList(3, teamInfos.size());
    if (teamInfos.size() == 4) {
      final var seasons = Short.parseShort(teamInfos.get(2));
      team.setRecord(teamInfos.get(1), seasons);
    } else if (teamInfos.size() == 2 && teamInfos.get(0).contains("Eingecheckt")) {
      SignupFactory.create(team, teamInfos.getFirst());
    }
  }

  private ModifyOutcome updateResult(@NotNull List<HTML> stages) {
    if (stages.isEmpty()) return ModifyOutcome.NOTHING;

    final String result = stages.getLast()
        .find("ul", HTML.ICON_INFO)
        .findAll("li").get(1).text().replace("Ergebnis", "");
    return team.setScore(determineDivision(stages), result);
  }

  private PRMLeague determineDivision(@NotNull List<HTML> stages) {
    final HTML content = stages.getLast().find("ul", HTML.ICON_INFO).find("li").find("a");
    return LeagueLoader.season(content.getAttribute("href"), content.text());
  }

}
