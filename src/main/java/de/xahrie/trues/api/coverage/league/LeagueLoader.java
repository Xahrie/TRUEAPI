package de.xahrie.trues.api.coverage.league;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.xahrie.trues.api.coverage.GamesportsLoader;
import de.xahrie.trues.api.coverage.league.model.PRMLeague;
import de.xahrie.trues.api.coverage.match.MatchFactory;
import de.xahrie.trues.api.coverage.match.MatchLoader;
import de.xahrie.trues.api.coverage.match.model.PRMMatch;
import de.xahrie.trues.api.coverage.playday.PlaydayFactory;
import de.xahrie.trues.api.coverage.season.PRMSeason;
import de.xahrie.trues.api.coverage.season.SeasonFactory;
import de.xahrie.trues.api.coverage.stage.model.PlayoffStage;
import de.xahrie.trues.api.coverage.team.TeamFactory;
import de.xahrie.trues.api.coverage.team.TeamLoader;
import de.xahrie.trues.api.coverage.team.model.PRMTeam;
import de.xahrie.trues.api.util.Const;
import de.xahrie.trues.api.util.StringUtils;
import de.xahrie.trues.api.util.exceptions.EntryMissingException;
import de.xahrie.trues.api.util.io.log.Console;
import de.xahrie.trues.api.util.io.log.DevInfo;
import de.xahrie.trues.api.util.io.request.HTML;
import de.xahrie.trues.api.util.io.request.URLType;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;

@ExtensionMethod(StringUtils.class)
public class LeagueLoader extends GamesportsLoader {
  public static PRMLeague season(@NotNull String url, @NotNull String name) {
    final int seasonId = url.between("/prm/", "-").intValue();
    final int stageId = stageIdFromUrl(url);
    final int divisionId = divisionIdFromUrl(url);
    final PRMSeason season = SeasonFactory.getSeason(seasonId);
    if (season == null)
      throw new EntryMissingException("Season " + seasonId + " wurde nicht erstellt.").info();
    return LeagueFactory.getGroup(season, name, stageId, divisionId);
  }

  public static String divisionNameFromURL(@NotNull String url) {
    String section = url.after("/", -1).after("-").replace("-", " ");
    if (section.startsWith("division ")) section = section.replaces(".", section.lastIndexOf(" "));
    return section.capitalizeFirst();
  }

  @NotNull
  public static Integer stageIdFromUrl(@NotNull String url) {
    return url.between((url.contains("/group/") ? "/group/" : "/playoff/"), "/").before("-").intValue();
  }

  @NotNull
  public static Integer divisionIdFromUrl(@NotNull String url) {
    return url.between("/", "-", 8).intValue();
  }

  private final PRMLeague league;
  private final String url;

  public LeagueLoader(@NotNull PRMLeague prmLeague) {
    super(prmLeague.getStage() instanceof PlayoffStage ? URLType.PLAYOFF : URLType.LEAGUE,
        prmLeague.getUrl().between("/prm/", "/").intValue(), stageIdFromUrl(prmLeague.getUrl()),
        prmLeague.getUrl().after("/", -1).intValue());
    this.url = prmLeague.getUrl();
    final int seasonId = url.between("/prm/", "/").intValue();
    final PRMSeason season = SeasonFactory.getSeason(seasonId);
    if (season == null) {
      new DevInfo("Season " + seasonId + " wurde nicht erstellt.").with(Console.class).warn();
      this.league = null;
    } else {
      final int divisionId = url.after("/", -1).intValue();
      this.league = LeagueFactory.getGroup(season, prmLeague.getName(), stageIdFromUrl(url), divisionId);
    }
  }

  public LeagueHandler load() {
    return LeagueHandler.builder()
        .url(url)
        .league(league)
        .teams(getTeams())
        .playdays(getPlaydays())
        .build();
  }

  @NotNull
  private List<PRMTeam> getTeams() {
    return html.find("tbody")
        .findAll("tr").stream()
        .map(row -> row.findAll("td").get(1))
        .map(cell -> cell.find("a").getAttribute("href"))
        .map(TeamLoader::idFromURL)
        .map(TeamFactory::getTeam)
        .toList();
  }

  @NotNull
  private List<LeaguePlayday> getPlaydays() {
    final String leagueName = html.find("h1").text().after(":");

    if (leagueName.equals(Const.Gamesports.STARTER_NAME)) {
      return List.of();
    }

    final List<LeaguePlayday> playdays = new ArrayList<>();
    final List<HTML> findAllByClass = html.findAll("div", HTML.PLAYDAY);
    for (int i = 0; i < findAllByClass.size(); i++) {
      final HTML playdayHTML = findAllByClass.get(i);
      final List<PRMMatch> primeMatches = playdayHTML.findAll("tr").stream()
          .map(match -> match.find("a").getAttribute("href"))
          .map(MatchLoader::idFromURL)
          .map(MatchFactory::getMatch)
          .filter(Objects::nonNull)
          .toList();
      final var playday = new LeaguePlayday(PlaydayFactory.getPlayday(league.getStage(), i + 1), primeMatches);
      playdays.add(playday);
    }
    return playdays;
  }
}
