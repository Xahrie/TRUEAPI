package de.xahrie.trues.api.scouting;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.xahrie.trues.api.coverage.match.model.Match;
import de.xahrie.trues.api.coverage.participator.model.Participator;
import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import de.xahrie.trues.api.coverage.team.model.Standing;
import de.xahrie.trues.api.database.query.Condition;
import de.xahrie.trues.api.database.query.JoinQuery;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.TimeFormat;
import de.xahrie.trues.api.datatypes.collections.SortedList;
import de.xahrie.trues.api.datatypes.number.TrueNumber;
import de.xahrie.trues.api.discord.builder.embed.EmbedFieldBuilder;
import de.xahrie.trues.api.riot.champion.Champion;
import de.xahrie.trues.api.riot.game.Game;
import de.xahrie.trues.api.riot.game.GameType;
import de.xahrie.trues.api.riot.game.Selection;
import de.xahrie.trues.api.riot.performance.Lane;
import de.xahrie.trues.api.riot.performance.Performance;
import de.xahrie.trues.api.riot.performance.TeamPerf;
import de.xahrie.trues.api.riot.KDA;
import de.xahrie.trues.api.util.Util;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class TeamAnalyzer extends AnalyzeManager {
  public TeamAnalyzer(Participator participator) {
    this(participator.getTeam(), participator.getTeamLineup().getValidPlayers());
  }

  public TeamAnalyzer(AbstractTeam team) {
    this(team, team.getPlayers());
  }

  private TeamAnalyzer(AbstractTeam team, List<Player> players) {
    super(team, players);
  }

  public TeamAnalyzer(AbstractTeam team, ScoutingGameType gameType, int days) {
    this(team, SortedList.of(), gameType, days);
  }

  private TeamAnalyzer(AbstractTeam team, List<Player> players, ScoutingGameType gameType, int days) {
    super(team, players, gameType, days);
  }

  public Map<Player, Integer> getLane(Lane lane) {
    return sort(players.stream().collect(Collectors.toMap(player -> player,
        player -> get(lane, player, gameType, days),
        (a, b) -> b, LinkedHashMap::new)));
  }

  public Map<Lane, Integer> getPlayer(Player player) {
    return Lane.ITERATE.stream().collect(Collectors.toMap(lane -> lane,
        lane -> get(lane, player, gameType, days),
        (a, b) -> b, LinkedHashMap::new));
  }

  private static Map<Player, Integer> sort(Map<Player, Integer> gamesOnLane) {
    return gamesOnLane.entrySet().stream()
        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toMap(
            Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
  }

  @Override
  public Query<Performance> performance() {
    return gameTypeString(new Query<>(Performance.class)
        .join(new JoinQuery<>(Performance.class, TeamPerf.class).col("t_perf"))
        .join(new JoinQuery<>(TeamPerf.class, Game.class))
        .join(new JoinQuery<>(Performance.class, Player.class))
        .where("_player.team", team).and(Condition.Comparer.GREATER_EQUAL, "_game.start_time", getStart()));
  }

  @Override
  public Query<Selection> selection() {
    final Query<Performance> performanceQuery = gameTypeString(new Query<>(Performance.class).distinct("_teamperf.game", Integer.class)
        .join(new JoinQuery<>(Performance.class, TeamPerf.class).col("t_perf"))
        .join(new JoinQuery<>(TeamPerf.class, Game.class))
        .join(new JoinQuery<>(Performance.class, Player.class))
        .where("_player.team", team).and(Condition.Comparer.GREATER_EQUAL, "_game.start_time", getStart()));
    return new Query<>(Selection.class).join(new JoinQuery<>(new Query<>(" inner join (" + performanceQuery.getSelectString(true) + ") as s1 on _selection.game = s1.game", performanceQuery.getParameters())));
  }

  @Override
  protected Query<Performance> gameTypeString(Query<Performance> query) {
    switch (gameType) {
      case PRM_ONLY -> query.where(Condition.Comparer.SMALLER_EQUAL, "_game.game_type", GameType.CUSTOM);
      case PRM_CLASH -> query.where(Condition.Comparer.SMALLER_EQUAL, "_game.game_type", GameType.CLASH);
      case TEAM_GAMES -> {
        if (team == null) query.where(Condition.Comparer.SMALLER_EQUAL, "_game.game_type", GameType.CLASH);
        else {
          query.join(new JoinQuery<>(new Query<>(" inner join ((SELECT DISTINCT t_perf FROM performance as `_performance` INNER JOIN `team_perf` as `_teamperf` ON `_performance`.`t_perf` = `_teamperf`.`team_perf_id` INNER JOIN `game` as `_game` ON `_teamperf`.`game` = `_game`.`game_id` INNER JOIN player as _player ON _performance.player = _player.player_id WHERE (_game.game_type <= 2 and _player.team = ?) LIMIT 1000) UNION DISTINCT (SELECT DISTINCT t_perf FROM performance as `_performance` INNER JOIN `team_perf` as `_teamperf` ON `_performance`.`t_perf` = `_teamperf`.`team_perf_id` INNER JOIN `game` as `_game` ON `_teamperf`.`game` = `_game`.`game_id` INNER JOIN `player` as `_player` ON `_performance`.`player` = `_player`.`player_id` WHERE (_game.game_type <= 3 and _player.team = ?) GROUP BY `t_perf`, `_player`.`team` HAVING count(`performance_id`) > 2 LIMIT 1000)) as j1 on _performance.t_perf = j1.t_perf", List.of(team, team))));
        }
      }
    }
    return query;
  }

  public List<ChampionData> handleChampions() {
    final List<Object[]> presence = selection().get("champion", Champion.class).get("count(selection_id)", Integer.class).groupBy("champion").descending("count(selection_id)").list();
    final List<Object[]> stats = performance().get("champion", Integer.class)
        .get("count(_performance.performance_id)", Integer.class).get("sum(if(_teamperf.win, 1, 0))", Integer.class)
        .get("sum(_performance.kills)", Integer.class).get("sum(_performance.deaths)", Integer.class)
        .get("sum(_performance.assists)", Integer.class)
        .groupBy("champion").descending("count(_performance.performance_id)").list();
    final Map<Champion, ChampionStats> championStats = stats.stream().collect(Collectors.toMap(
        stat -> new Query<>(Champion.class).entity(stat[0]),
        stat -> new ChampionStats(new Standing(((BigDecimal) stat[2]).intValue(), ((Long) stat[1]).intValue() - ((BigDecimal) stat[2]).intValue()),
            new KDA(((BigDecimal) stat[3]).shortValue(), ((BigDecimal) stat[4]).shortValue(), ((BigDecimal) stat[5]).shortValue())), (a, b) -> b));
    final Object[] games = gameType.teamQuery(team, days).performance().get("count(distinct _teamperf.game)", Integer.class).single();
    final Long amountOfGames = (Long) games[0];
    return presence.stream().map(objs -> new ChampionData((Champion) objs[0], (int) objs[1] * 1. / amountOfGames, championStats.get((Champion) objs[0]))).toList();

  }

  public List<Match> getMatches() {
    final LocalDateTime startTime = LocalDateTime.now().minusDays(days);
    return new Query<>(Participator.class).get("coverage", Match.class)
        .join(new JoinQuery<>(Participator.class, Match.class).col("coverage"))
        .keep("team", team)
        .where(Condition.Comparer.GREATER_EQUAL, "_match.coverage_start", startTime).or("_match.result", "-:-")
        .ascending("_match.coverage_start").convertList(Match.class);
  }

  public List<MessageEmbed.Field> analyzeHistory(int page) {
    final List<MessageEmbed.Field> fields = new ArrayList<>();

    final List<TeamPerf> games = performance().get("t_perf", TeamPerf.class).descending("_game.start_time").convertList(TeamPerf.class);
    if (games.size() <= (page - 1) * 6) page = (int) Math.ceil(games.size() / 6.);

    for (int i = 0; i < 6; i++) {
      final int count = i + (page - 1) * 6;
      if (count >= games.size()) break;
      fields.addAll(getFieldsOfTeamPerformance(games.get(count)));
    }
    return fields;
  }

  private List<MessageEmbed.Field> getFieldsOfTeamPerformance(TeamPerf teamPerf) {
    final List<MessageEmbed.Field> fields = new ArrayList<>();

    final String head = TimeFormat.DISCORD.of(teamPerf.getGame().getStart()) + teamPerf.getGame().getType().name() + ": " + teamPerf.getWinString() +
                        Util.avoidNull(teamPerf.getOpposingTeam(), "", primeTeam -> " vs " + primeTeam.getName());
    final String description = "(" + teamPerf.getKda().toString() + ") nach " + teamPerf.getGame().getDuration();
    fields.add(new MessageEmbed.Field(head, description, false));

    return new EmbedFieldBuilder<>(fields, teamPerf.getPerformances().stream().sorted().toList())
        .add("Spielername", Performance::getPlayername)
        .add("Matchup", performance -> performance.getMatchup().toString())
        .add("Stats", Performance::getStats)
        .build();
  }

  public record ChampionData(Champion champion, double presence, ChampionStats stats) {
    public String getPicksString() {
      return new TrueNumber(presence).percentValue() + " - " + stats.standing.toString();
    }

    public String getKDAString() {
      return stats.kda.kills() + " / " + stats.kda.deaths() + " / " + stats.kda.assists();
    }
  }

  public record ChampionStats(Standing standing, KDA kda) {
  }
}
