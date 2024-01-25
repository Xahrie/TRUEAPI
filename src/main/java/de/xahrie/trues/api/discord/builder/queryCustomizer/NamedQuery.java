package de.xahrie.trues.api.discord.builder.queryCustomizer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import de.xahrie.trues.api.coverage.league.model.AbstractLeague;
import de.xahrie.trues.api.coverage.league.model.PRMLeague;
import de.xahrie.trues.api.coverage.match.model.Match;
import de.xahrie.trues.api.coverage.participator.model.Participator;
import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.coverage.player.model.PlayerRank;
import de.xahrie.trues.api.coverage.player.model.Rank;
import de.xahrie.trues.api.coverage.stage.model.Stage;
import de.xahrie.trues.api.coverage.team.leagueteam.LeagueTableDTO;
import de.xahrie.trues.api.coverage.team.leagueteam.LeagueTeam;
import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import de.xahrie.trues.api.coverage.team.model.PRMTeam;
import de.xahrie.trues.api.discord.command.slash.Column;
import de.xahrie.trues.api.discord.command.slash.DBQuery;
import de.xahrie.trues.api.community.application.Application;
import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.coverage.playday.Playday;
import de.xahrie.trues.api.coverage.season.OrgaCupSeason;
import de.xahrie.trues.api.coverage.season.PRMSeason;
import de.xahrie.trues.api.coverage.season.SeasonFactory;
import de.xahrie.trues.api.database.query.Condition;
import de.xahrie.trues.api.database.query.Formatter;
import de.xahrie.trues.api.database.query.JoinQuery;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.TimeFormat;
import de.xahrie.trues.api.datatypes.collections.SortedList;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.logging.MessageLog;
import de.xahrie.trues.api.riot.champion.Champion;
import de.xahrie.trues.api.riot.game.Game;
import de.xahrie.trues.api.riot.game.GameType;
import de.xahrie.trues.api.riot.game.Selection;
import de.xahrie.trues.api.riot.performance.Performance;
import de.xahrie.trues.api.riot.performance.TeamPerf;
import de.xahrie.trues.api.util.Format;
import de.xahrie.trues.api.util.StringUtils;
import de.xahrie.trues.api.util.Util;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
@Getter
@ExtensionMethod(StringUtils.class)
public enum NamedQuery {
  CHAMPIONS(new Query<>(Selection.class, 50)
      .get("_champion.champion_name as Champion1", String.class)
      .get("concat(round(count(*) * 100 / (SELECT count(DISTINCT game) FROM selection JOIN game ON selection.game = game.game_id WHERE game_type = 0 AND orgagame = true)), '% - ', (SELECT concat(SUM(team_perf.win), ' : ', SUM(NOT team_perf.win)) FROM performance JOIN player ON player = player_id JOIN team_perf ON t_perf = team_perf_id JOIN game ON game = game_id JOIN champion ON performance.champion = champion.champion_id WHERE game_type = 0 and orgagame = true AND champion.champion_name = Champion1))", String.class)
      .get("(SELECT concat(SUM(performance.kills), ' / ', SUM(performance.deaths), ' / ', SUM(performance.assists)) FROM performance JOIN player ON player = player_id JOIN team_perf ON t_perf = team_perf_id JOIN game ON game = game_id JOIN champion ON performance.champion = champion.champion_id WHERE game_type = 0 and orgagame = true AND champion.champion_name = Champion1)", String.class)
      .join(new JoinQuery<>(Selection.class, Game.class))
      .join(new JoinQuery<>(Selection.class, Champion.class))
      .where("_game.game_type", GameType.TOURNAMENT).and("_game.orgagame", true)
      .groupBy("_selection.champion").descending("count(*)"),
      new DBQuery("Rekordchampions", "nur Prime League Spiele", List.of(
              new Column("Championname", 20), new Column("Picks", 20), new Column("KDA", 15)),
                  Enumeration.CONTINUE), null),
  FACED_TEAMS(new Query<>("SELECT * FROM team WHERE 0"), new DBQuery("bisherige Gegner & Scrimteams", "Alle bisherigen Prime League Gegner und Scrimteams der Orga", List.of(
      new Column("Placeholder", 50), new Column("Team 2", 20), new Column("Standing", 9))), new Alternative(0,
      List.of("3. Liga", "4. Liga", "5. Liga", "6. Liga", "7. Liga", "8. Liga", "Starter Liga"))),
  SEASON_CHAMPIONS(new Query<>(Selection.class, 50)
      .get("_champion.champion_name as Champion1", String.class)
      .get("concat(round(count(*) * 100 / (SELECT count(DISTINCT game) FROM selection JOIN game ON selection.game = game.game_id WHERE game_type = 0 AND orgagame = true)), '% - ', (SELECT concat(SUM(team_perf.win), ' : ', SUM(NOT team_perf.win)) FROM performance JOIN player ON player = player_id JOIN team_perf ON t_perf = team_perf_id JOIN game ON game = game_id JOIN champion ON performance.champion = champion.champion_id WHERE game_type = 0 and orgagame = true AND champion.champion_name = Champion1))", String.class)
      .get("(SELECT concat(SUM(performance.kills), ' / ', SUM(performance.deaths), ' / ', SUM(performance.assists)) FROM performance JOIN player ON player = player_id JOIN team_perf ON t_perf = team_perf_id JOIN game ON game = game_id JOIN champion ON performance.champion = champion.champion_id WHERE game_type = 0 and orgagame = true AND champion.champion_name = Champion1)", String.class)
      .join(new JoinQuery<>(Selection.class, Game.class))
      .join(new JoinQuery<>(Selection.class, Champion.class))
      .where("_game.game_type", GameType.TOURNAMENT).and("_game.orgagame", true)
      .and(Condition.Comparer.GREATER_EQUAL, "_game.start_time",
          new Query<>(PRMSeason.class).where("season_start <= now()").descending("season_start").entity().getRange().getStartTime())
      .groupBy("_selection.champion").descending("count(*)"),
      new DBQuery("Rekordchampions", "nur Prime League Spiele", List.of(
          new Column("Championname", 20), new Column("Picks", 20), new Column("KDA", 15)),
          Enumeration.CONTINUE), null),
  ORGA_ELOS(new Query<>("SELECT * FROM player WHERE 0"), new DBQuery("Elo-Leaderboard", "Das Elo-Leaderboard (imagine Emerald sein KEKW)", List.of(
      new Column("League-Account", 25), new Column("Teamname", 30), new Column("Punkte", 11)),
      Enumeration.CONTINUE), new Alternative(2, List.of("Challenger", "Grandmaster", "Master", "Diamond", "Emerald", "Platinum", "Gold", "Silver", "Bronze", "Iron"), true)),
  ORGA_SCHEDULE(new Query<>("SELECT * FROM player WHERE 0"), new DBQuery("Orga-Zeitplan", "Spiele dieses Splits", List.of(
      new Column("Team 1", 30), new Column("Team 2", 30), new Column("Zeit/Ergebnis"))), null),
  ORGA_PRM(new Query<>(Performance.class, 100)
      .get("_player.lol_name as Leaguename", String.class)
      .get("count(*)", Integer.class)
      .get("concat(round(avg(_teamperf.win)*100), '%')", String.class)
      .get("concat(sum(_performance.kills), ' / ', sum(_performance.deaths), ' / ', sum(_performance.assists))", String.class)
      .get("(sum(_performance.kills) + sum(_performance.assists)) / sum(_performance.deaths)", Double.class)
      .get("sum(creeps) / 1000", Double.class)
      .get("sum(creeps) / count(*)", Double.class)
      .get("ROUND((AVG(creeps) - (SELECT AVG(creeps) FROM performance JOIN player ON player = player_id JOIN team_perf ON t_perf = team_perf_id WHERE (game, lane, first) IN (SELECT game, lane, NOT first FROM performance JOIN player ON player = player_id JOIN team_perf ON t_perf = team_perf_id JOIN game ON game = game_id WHERE orgagame = true AND lol_name = Leaguename))))", Integer.class)
      .get("sum(gold) / 1000", Double.class)
      .get("sum(gold) / (count(*)*1000)", Double.class)
      .get("ROUND((AVG(gold) - (SELECT AVG(gold) FROM performance JOIN player ON player = player_id JOIN team_perf ON t_perf = team_perf_id WHERE (game, lane, first) IN (SELECT game, lane, NOT first FROM performance JOIN player ON player = player_id JOIN team_perf ON t_perf = team_perf_id JOIN game ON game = game_id WHERE orgagame = true AND lol_name = Leaguename))))", Integer.class)
      .get("sum(damage) / 1000", Double.class)
      .get("sum(damage) / (count(*)*1000)", Double.class)
      .get("ROUND((AVG(damage) - (SELECT AVG(damage) FROM performance JOIN player ON player = player_id JOIN team_perf ON t_perf = team_perf_id WHERE (game, lane, first) IN (SELECT game, lane, NOT first FROM performance JOIN player ON player = player_id JOIN team_perf ON t_perf = team_perf_id JOIN game ON game = game_id WHERE orgagame = true AND lol_name = Leaguename))))", Integer.class)
      .get("sum(vision)", Double.class)
      .get("sum(vision) / count(*)", Double.class)
      .get("ROUND((AVG(vision) - (SELECT AVG(vision) FROM performance JOIN player ON player = player_id JOIN team_perf ON t_perf = team_perf_id WHERE (game, lane, first) IN (SELECT game, lane, NOT first FROM performance JOIN player ON player = player_id JOIN team_perf ON t_perf = team_perf_id JOIN game ON game = game_id WHERE orgagame = true AND lol_name = Leaguename))))", Integer.class)
      .join(new JoinQuery<>(Performance.class, TeamPerf.class).col("t_perf"))
      .join(new JoinQuery<>(TeamPerf.class, Game.class).col("_teamperf.game"))
      .join(new JoinQuery<>(Performance.class, Player.class))
      .where("_game.game_type", GameType.TOURNAMENT).and("_player.played", true)
      .groupBy("_player.player_id").descending("count(*)"),
      new DBQuery("Rekordspieler", "nur Prime League Spiele",
          List.of(
              new Column("League-Account"),
              new Column("Games").round(),
              new Column("").withPrevious().round(),
              new Column("KDA-Ratio").round(),
              new Column("").withPrevious().round(1),
              new Column("CS in Tsd.").round(1),
              new Column("").withPrevious().round(),
              new Column("CSD").round(),
              new Column("Gold in Tsd.").round(),
              new Column("").withPrevious().round(1),
              new Column("GoldD").round(),
              new Column("Dmg in Tsd.").round(),
              new Column("").withPrevious().round(1),
              new Column("DmgD").round(),
              new Column("Vision").round(),
              new Column("").withPrevious().round(),
              new Column("VSD").round(1)
          ),
          Enumeration.CONTINUE), null
  ),
  TRYOUTS(new Query<>(Application.class, 50)
      .join(new JoinQuery<>(Application.class, DiscordUser.class).col("discord_user"))
      .get("_discorduser.mention", String.class)
      .get(" - ", Formatter.of("lineup_role"), Formatter.of("position"))
      .get("waiting", Boolean.class)
      .where(Condition.notNull("waiting"))
      .descending("waiting"),
      new DBQuery("", "", List.of(new Column("Nutzer"), new Column("Rolle"), new Column("wartend")), Enumeration.CONTINUE), null),


  PROFILE_RIOT_ACCOUNT(null, new DBQuery("", "", List.of(new Column("Riot-Account")), Enumeration.CONTINUE), null),
  PROFILE_PRM_TEAM(null, new DBQuery("", "", List.of(new Column("Team"), new Column("Infos")), Enumeration.CONTINUE), null),
  PROFILE_PRM_GAMES(new Query<>(Performance.class, 10)
      .join(new JoinQuery<>(Performance.class, TeamPerf.class).col("t_perf"))
      .join(new JoinQuery<>(TeamPerf.class, Game.class).col("_teamperf.game"))
      .join(new JoinQuery<>(Performance.class, Champion.class).as("my"))
      .join(new JoinQuery<>(Performance.class, Champion.class).col("enemy_champion").as("enemy"))
      .join(new JoinQuery<>(Performance.class, Player.class))
      .get(" - ", Formatter.of("_game.start_time", Formatter.CellFormat.AUTO),
          Formatter.of("IF(lane = 1, 'Top', IF(lane = 2, 'Jgl', IF(lane = 3, 'Mid', IF(lane = 4, 'Bot', IF(lane = 5, 'Sup', 'none')))))"))
      .get(" vs ", Formatter.of("_my.champion_name"), Formatter.of("_enemy.champion_name"))
      .get(" / ", Formatter.of("kills"), Formatter.of("deaths"),
          Formatter.of("CONCAT(_performance.assists, ' (', IF(_teamperf.win = false, 'N', 'S'), ')')"))
      .where("_game.game_type", GameType.TOURNAMENT).and("_player.lol_puuid", "?")
      .descending("_game.start_time"),
      new DBQuery("Prime League Spiele", "letzte 10 Spiele in der Prime League", List.of(
          new Column("Spielzeit"), new Column("Matchup"), new Column("Stats")), Enumeration.CONTINUE), null),
  STRIKES(new Query<>(MessageLog.class, 25)
      .get(null, Formatter.of("log_time", Formatter.CellFormat.AUTO))
      .get("reason", String.class)
      .get("if(discord_channel is not null, 'Nachricht', 'Strike')", String.class)
      .where("target", "?")
      .descending("log_time"),
      new DBQuery("Letzte Strikes", "letzte Strikes", List.of(
          new Column("Datum"), new Column("Grund"), new Column("Typ")), Enumeration.CONTINUE), null),
  TABLE(new Query<>("SELECT * FROM league_team WHERE 0"), new DBQuery("Prime League Tabellen", "Da gibt es ein paar Tabellen", List.of(
      new Column("#", 1),
      new Column("Teamname", 20),
      new Column("Score"),
      new Column("Kills"),
      new Column("KD"),
      new Column("Gold"),
      new Column("GD"),
      new Column("Damage"),
      new Column("DD"),
      new Column("Creeps"),
      new Column("CD"),
      new Column("Vision"),
      new Column("VD"),
      new Column("Objectives"),
      new Column("Spielzeit")
  ), Enumeration.NONE), null);

  private final Query<?> query;
  private final DBQuery dbQuery;
  private final Alternative alternative;

  @Nullable
  public List<List<Object[]>> getCustom() {
    return switch (this) {
      case ORGA_SCHEDULE -> handleOrgaGames();
      case ORGA_ELOS -> handleOrgaElo();
      case FACED_TEAMS -> handleFacedTeams();
      case TABLE -> getTables();
      default -> null;
    };
  }

  private List<List<Object[]>> handleFacedTeams() {
    return List.of(
        getTeamsOfDivision("Gruppe 3"),
        getTeamsOfDivision("Gruppe 4"),
        getTeamsOfDivision("Gruppe 5"),
        getTeamsOfDivision("Gruppe 6"),
        getTeamsOfDivision("Gruppe 7"),
        getTeamsOfDivision("Gruppe 8"),
        getTeamsOfDivision("Starter")
    );
  }

  private List<Object[]> getTeamsOfDivision(String start) {
    return new Query<>(LeagueTeam.class)
        .get("_team.team_name", String.class)
        .get("CONCAT(_leagueteam.current_place, ': (', _leagueteam.current_wins, ':', _leagueteam.current_losses, ')')", String.class)
        .get("CONCAT(_team.total_wins, ' : ', _team.total_losses)", String.class)
        .join(new JoinQuery<>(LeagueTeam.class, AbstractLeague.class))
        .join(new JoinQuery<>(AbstractLeague.class, Stage.class))
        .join(new JoinQuery<>(LeagueTeam.class, AbstractTeam.class))
        .where("_stage.season", SeasonFactory.getLastPRMSeason()).and("_team.highlight", true)
        .and(Condition.Comparer.LIKE, "_league.group_name", start + "%")
        .ascending("`_leagueteam`.`current_place` * 100 - `_leagueteam`.`current_wins`")
        .list();
  }

  @NotNull
  private List<List<Object[]>> handleOrgaGames() {
    final PRMSeason lastPRMSeason = SeasonFactory.getLastPRMSeason();
    assert lastPRMSeason != null;
    final OrgaCupSeason lastInternSeason = SeasonFactory.getLastInternSeason();
    assert lastInternSeason != null;
    final List<Match> matches = new Query<>(Participator.class).get("_participator.coverage", Match.class)
                                                               .join(new JoinQuery<>(Participator.class, OrgaTeam.class).col("team").ref("team"))
                                                               .join(new JoinQuery<>(Participator.class, Match.class).col("coverage"))
                                                               .where(Condition.Comparer.NOT_EQUAL, "_match.department", "scrimmage")
                                                               .and(Condition.Comparer.GREATER_EQUAL, "_match.coverage_start", lastPRMSeason.getRange().getStartTime())
                                                               .include(new Query<>(Match.class).where("_match.department", "intern")
            .and(Condition.Comparer.GREATER_EQUAL, "_match.coverage_start", lastInternSeason.getRange().getStartTime()))
                                                               .convertList(Match.class);
    final Map<Playday, List<Match>> matchData = new HashMap<>();
    for (final Match match : matches) {
      if (!matchData.containsKey(match.getPlayday())) matchData.put(match.getPlayday(), new ArrayList<>());
      matchData.get(match.getPlayday()).add(match);
    }
    return matchData.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(entry -> entry.getValue().stream().sorted(Match::compareTo).map(match -> new Object[]{
        Util.avoidNull(match.getHome().getTeam(), "null", team -> team.getName().keep(25)),
        Util.avoidNull(match.getGuest().getTeam(), "null", team -> team.getName().keep(25)),
        match.getResult().toString().equals("-:-") ? (TimeFormat.AUTO.of(match.getStart()) + (match.getCast() == null ? "" : " (c)")) : match.getResult().toString()
    }).toList()).toList();
  }

  private List<List<Object[]>> handleOrgaElo() {
    final int amount = Rank.RankTier.values().length - 1;
    final List<PlayerRank>[] ranks = new SortedList[amount];
    IntStream.range(1, Rank.RankTier.values().length).forEach(i -> ranks[i - 1] = SortedList.sorted(Comparator.comparing(PlayerRank::getRank)));

    for (Player player : new Query<>(Player.class).where(Condition.notNull("_player.discord_user")).entityList()) {
      final PlayerRank current = player.getRanks().getCurrent();
      final Rank.RankTier tier = current.getRank().tier();
      if (!tier.equals(Rank.RankTier.UNRANKED)) ranks[Rank.RankTier.CHALLENGER.ordinal() - tier.ordinal()].add(current);
    }
    final List<List<Object[]>> out =  new ArrayList<>();
    for (List<PlayerRank> rank : ranks) {
      final List<Object[]> list = rank.stream().map(playerRank -> new Object[]{
          playerRank.getPlayer().getName().toString(),
          Util.avoidNull(playerRank.getPlayer().getTeam(), "null", AbstractTeam::getName).keep(25),
          "Tier " + playerRank.getRank().division().ordinal() + " " + playerRank.getRank().points() + " LP (" + playerRank.getWinrate().format(Format.MEDIUM) + ")"
      }).toList();
      out.add(list);
    }
    return out;
  }

  private static List<List<Object[]>> getTables() {
    final List<List<Object[]>> list = SortedList.of();
    for (OrgaTeam orgaTeam : new Query<>(OrgaTeam.class).ascending("orga_place").entityList()) {
      final AbstractTeam team = orgaTeam.getTeam();
      if (!(team instanceof PRMTeam prmTeam)) continue;

      final PRMLeague lastLeague = prmTeam.getLastLeague();
      if (lastLeague == null) continue;

      final List<LeagueTableDTO> sorted = SortedList.sorted(new Query<>(LeagueTeam.class).where("league", lastLeague)
                                                                                         .ascending("current_place").entityList().stream().map(LeagueTableDTO::new));
      final List<Object[]> objects = sorted.stream().map(leagueTableDTO -> leagueTableDTO.getData().toArray(Object[]::new)).toList();
      list.add(objects);
    }
    return list;
  }
}
