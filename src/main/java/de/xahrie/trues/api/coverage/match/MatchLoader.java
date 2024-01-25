package de.xahrie.trues.api.coverage.match;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import de.xahrie.trues.api.coverage.GamesportsLoader;
import de.xahrie.trues.api.coverage.league.LeagueFactory;
import de.xahrie.trues.api.coverage.league.LeagueLoader;
import de.xahrie.trues.api.coverage.league.model.PRMLeague;
import de.xahrie.trues.api.coverage.match.model.PRMMatch;
import de.xahrie.trues.api.coverage.playday.Playday;
import de.xahrie.trues.api.coverage.playday.PlaydayFactory;
import de.xahrie.trues.api.coverage.playday.config.SchedulingRange;
import de.xahrie.trues.api.coverage.playday.scheduler.PlaydayScheduler;
import de.xahrie.trues.api.coverage.player.model.PlayerExperience;
import de.xahrie.trues.api.coverage.season.PRMSeason;
import de.xahrie.trues.api.coverage.team.TeamFactory;
import de.xahrie.trues.api.coverage.team.TeamLoader;
import de.xahrie.trues.api.coverage.team.model.PRMTeam;
import de.xahrie.trues.api.database.connector.Database;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.DateTimeUtils;
import de.xahrie.trues.api.datatypes.collections.SortedList;
import de.xahrie.trues.api.util.StringUtils;
import de.xahrie.trues.api.util.exceptions.EntryMissingException;
import de.xahrie.trues.api.util.io.request.HTML;
import de.xahrie.trues.api.util.io.request.URLType;
import lombok.Getter;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;

@Getter
@ExtensionMethod(StringUtils.class)
public class MatchLoader extends GamesportsLoader {
  public static Integer idFromURL(String url) {
    return url.between("/matches/", "-").intValue();
  }

  private PRMMatch match;

  public MatchLoader(@NotNull PRMMatch match) {
    super(URLType.MATCH, match.getMatchId());
    this.match = match;
  }

  public MatchLoader(int matchId) {
    super(URLType.MATCH, matchId);
  }

  public MatchLoader loadParticipatingPlayers() {
    if (html.text() == null) {
      System.err.println("SEITE NICHT GEFUNDEN!");
      return null;
    }


    final String format = html.find("div", HTML.MATCH_SCORE).find("div", HTML.DESCRIPTION).text();
    int games = switch (format) {
      case "one game" -> 1;
      case "two games" -> 2;
      case "best of three" -> 3;
      case "best of five" -> 5;
      default -> throw new IllegalArgumentException(format);
    };
    SortedList<Integer> players = SortedList.of();
    List<Integer> teamsInserted = SortedList.of();

    for (final HTML logEntry : html.findAll("tr")) {
      List<HTML> cells = logEntry.findAll("td");
      if (cells.isEmpty()) continue; // entferne Head

      String action = cells.get(2).text();
      if (!action.equals("lineup_submit")) continue;

      int teamId = cells.get(1).text().between("(Team ", ")").intValue();
      if (teamId == -1) {
        System.err.println("Ungültige Teamid");
        return null;
      }

      if (teamsInserted.contains(teamId)) continue;

      teamsInserted.add(teamId);
      String details = cells.get(3).text();
      players.addAll(
          Arrays.stream(details.split(", "))
              .map(playerString -> playerString.before(":").intValue())
              .toList()
      );
    }

    players.forEach(playerid -> PlayerExperience.of(playerid).addGames(games));
    if (!players.isEmpty())
      Database.connection().commit();
    System.out.println("Es wurden " + players.size() + " Spieler für Match " + super.id + " gefunden.");
    return this;
  }

  public MatchLoader create() {
    final String seasonName = html.find("h1").text().before(":");
    final PRMSeason season = new Query<>(PRMSeason.class).where("season_full", seasonName).entity();
    if (season == null)
      throw new EntryMissingException("Season " + seasonName + " wurde nicht erstellt.").info();

    final HTML division = html.find("ul", "breadcrumbs").findAll("li").get(2);
    final String divisionName = division.text();
    final String divisionURL = division.find("a").getAttribute("href");
    final int stageId = LeagueLoader.stageIdFromUrl(divisionURL);
    final int divisionId = LeagueLoader.divisionIdFromUrl(divisionURL);
    final PRMLeague league = LeagueFactory.getGroup(season, divisionName.strip(), stageId, divisionId);

    final Playday playday = getPlayday(league);
    final PlaydayScheduler scheduler = PlaydayScheduler.create(league.getStage(), playday.getIdx(), league.getTier());
    SchedulingRange scheduling = scheduler.scheduling();
    this.match = new PRMMatch(playday, getMatchtime(), league, scheduling, this.id).create();
    return this;
  }

  public MatchHandler load() {
    return MatchHandler.builder()
        .html(html)
        .url(url)
        .match(match)
        .teams(getTeams())
        .logs(html.findAll("tr")).build();
  }

  private Playday getPlayday(PRMLeague league) {
    final List<HTML> data = html.find("div", HTML.MATCH_SUBTITLE)
        .findAll("div", HTML.SUBTITLE);
    if (data.size() < 2) {
      return PlaydayFactory.fromMatchtime(league.getStage(), getMatchtime());
    }
    final String playdayName = data.get(1).text().trim();
    final int index = playdayName.equals("Tiebreaker") ? 8 : playdayName.split(" ")[1].intValue();
    return PlaydayFactory.getPlayday(league.getStage(), index);
  }

  private LocalDateTime getMatchtime() {
    final int epochSeconds = html.find("span", HTML.MATCH_TIME).getAttribute(HTML.TIME_ATTRIBUTE).intValue();
    return DateTimeUtils.fromEpoch(epochSeconds);
  }

  private List<PRMTeam> getTeams() {
    return html.findAll("div", HTML.MATCH_TEAMS).stream()
        .map(team -> team.find("a").getAttribute("href"))
        .map(TeamLoader::idFromURL)
        .map(TeamFactory::getTeam)
        .toList();
  }

}
