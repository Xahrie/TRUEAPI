package de.xahrie.trues.api.coverage.match;

import java.time.LocalDateTime;
import java.util.List;

import de.xahrie.trues.api.coverage.league.LeagueFactory;
import de.xahrie.trues.api.coverage.league.model.PRMLeague;
import de.xahrie.trues.api.coverage.match.model.PRMMatch;
import de.xahrie.trues.api.coverage.playday.Playday;
import de.xahrie.trues.api.coverage.playday.PlaydayFactory;
import de.xahrie.trues.api.coverage.playday.config.SchedulingRange;
import de.xahrie.trues.api.coverage.playday.scheduler.PlaydayScheduler;
import de.xahrie.trues.api.coverage.season.PRMSeason;
import de.xahrie.trues.api.coverage.team.model.PRMTeam;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.DateTimeUtils;
import de.xahrie.trues.api.coverage.GamesportsLoader;
import de.xahrie.trues.api.coverage.team.TeamFactory;
import de.xahrie.trues.api.coverage.team.TeamLoader;
import de.xahrie.trues.api.util.StringUtils;
import de.xahrie.trues.api.util.io.log.Console;
import de.xahrie.trues.api.util.io.log.DevInfo;
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

  MatchLoader(int matchId) {
    super(URLType.MATCH, matchId);
  }

  MatchLoader create() {
    final String seasonName = html.find("h1").text().before(":");
    final PRMSeason season = new Query<>(PRMSeason.class).where("season_full", seasonName).entity();
    if (season == null) {
      new DevInfo("Season wurde nicht erstellt.").with(Console.class).warn();
      return null;
    }

    final HTML division = html.find("ul", "breadcrumbs").findAll("li").get(2);
    final String divisionName = division.text();
    final String divisionURL = division.find("a").getAttribute("href");
    final int stageId = divisionURL.between("/group/", "-").intValue();
    final int divisionId = divisionURL.between("/", "-", 8).intValue();
    final PRMLeague
            league = LeagueFactory.getGroup(season, divisionName.strip(), stageId, divisionId);

    final Playday playday = getPlayday(league);
    final PlaydayScheduler
            playdayScheduler = PlaydayScheduler.create(league.getStage(), playday.getIdx(), league.getTier());
    final SchedulingRange scheduling = playdayScheduler.scheduling();
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
    final String playdayName = data.get(1).text();
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
