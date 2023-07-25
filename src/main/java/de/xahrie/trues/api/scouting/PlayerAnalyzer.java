package de.xahrie.trues.api.scouting;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.coverage.team.model.Standing;
import de.xahrie.trues.api.database.query.Condition;
import de.xahrie.trues.api.database.query.JoinQuery;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.database.query.SQLGroup;
import de.xahrie.trues.api.discord.builder.embed.EmbedFieldBuilder;
import de.xahrie.trues.api.riot.champion.Champion;
import de.xahrie.trues.api.riot.game.Game;
import de.xahrie.trues.api.riot.game.GameType;
import de.xahrie.trues.api.riot.game.Selection;
import de.xahrie.trues.api.riot.performance.Lane;
import de.xahrie.trues.api.riot.performance.Performance;
import de.xahrie.trues.api.riot.performance.PlayerMatchHistoryPerformanceDTO;
import de.xahrie.trues.api.riot.performance.TeamPerf;
import de.xahrie.trues.api.util.Util;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerAnalyzer extends AnalyzeManager {
  public PlayerAnalyzer(Player player) {
    super(player.getTeam(), List.of(player));
  }

  public PlayerAnalyzer(Player player, ScoutingGameType gameType, int days) {
    super(player.getTeam(), List.of(player), gameType, days);
  }

  private Player player() {
    return players.get(0);
  }

  public Map<Lane, Integer> getPlayerLanes() {
    return Lane.ITERATE.stream().collect(Collectors.toMap(lane -> lane,
        lane -> AnalyzeManager.get(lane, players.get(0), gameType, days),
        (a, b) -> b, LinkedHashMap::new));
  }

  public int getLane(Lane lane) {
    return getPlayerLanes().get(lane);
  }

  @Override
  public Query<Performance> performance() {
    return gameTypeString(new Query<>(Performance.class)
        .join(new JoinQuery<>(Performance.class, TeamPerf.class).col("t_perf"))
        .join(new JoinQuery<>(TeamPerf.class, Game.class))
        .join(new JoinQuery<>(Performance.class, Player.class))
        .where("player", player()).and(Condition.Comparer.GREATER_EQUAL, "_game.start_time", getStart()));
  }

  @Override
  public Query<Selection> selection() {
    final Query<Performance> performanceQuery = gameTypeString(new Query<>(Performance.class).distinct("_teamperf.game", Integer.class)
        .join(new JoinQuery<>(Performance.class, TeamPerf.class).col("t_perf"))
        .join(new JoinQuery<>(TeamPerf.class, Game.class))
        .join(new JoinQuery<>(Performance.class, Player.class))
        .where("player", player()).and(Condition.Comparer.GREATER_EQUAL, "_game.start_time", getStart()));
    return new Query<>(Selection.class).join(new JoinQuery<>(new Query<>(" inner join (" + performanceQuery.getSelectString(true) + ") as s1 on _selection.game = s1.game", performanceQuery.getParameters())));
  }

  @Override
  protected Query<Performance> gameTypeString(Query<Performance> query) {
    switch (gameType) {
      case PRM_ONLY -> query.where(Condition.Comparer.SMALLER_EQUAL, "_game.game_type", GameType.CUSTOM);
      case PRM_CLASH -> query.where(Condition.Comparer.SMALLER_EQUAL, "_game.game_type", GameType.CLASH);
      case TEAM_GAMES ->
          query.join(new JoinQuery<>(new Query<>(" inner join ((SELECT DISTINCT t_perf FROM performance as `_performance` INNER JOIN `team_perf` as `_teamperf` ON `_performance`.`t_perf` = `_teamperf`.`team_perf_id` INNER JOIN `game` as `_game` ON `_teamperf`.`game` = `_game`.`game_id` WHERE (_game.game_type <= 2 and player = ?) LIMIT 1000) UNION DISTINCT (SELECT DISTINCT t_perf FROM performance as `_performance` INNER JOIN `team_perf` as `_teamperf` ON `_performance`.`t_perf` = `_teamperf`.`team_perf_id` INNER JOIN `game` as `_game` ON `_teamperf`.`game` = `_game`.`game_id` INNER JOIN `player` as `_player` ON `_performance`.`player` = `_player`.`player_id` WHERE (_game.game_type <= 3 and _player.team = ?) GROUP BY `t_perf`, `_player`.`team` HAVING count(`performance_id`) > 2 LIMIT 1000)) as j1 on _performance.t_perf = j1.t_perf", List.of(player(), Util.avoidNull(player().getTeam(), 0)))));
    }
    return query;
  }

  public List<MessageEmbed.Field> analyzePicks(Lane lane) {
    final List<MessageEmbed.Field> fields = new ArrayList<>();
    fields.add(getPlayerHeadField(lane));
    final var data = new EmbedFieldBuilder<>(handlePicks())
        .add("Champion", PlayerAnalyzerData::getChampionString)
        .add("Competitive", PlayerAnalyzerData::getCompetitiveString)
        .add("Alle Games", PlayerAnalyzerData::getMatchmadeString);
    fields.addAll(data.build());
    return fields;
  }

  public List<MessageEmbed.Field> analyzeGamesWith(@Nullable
  Champion champion, @Nullable Lane lane) {
    final List<List<String>> entries = PlayerMatchHistoryPerformanceDTO.get(player(), gameType, lane, champion).entityList(25).stream().map(performance -> new PlayerMatchHistoryPerformanceDTO(performance).getStrings()).toList();
    final var data = new EmbedFieldBuilder<>(entries)
        .add("Zeitpunkt", entry -> entry.get(0))
        .add("Matchup", entry -> entry.get(1))
        .add("KDA", entry -> entry.get(2));
    return new ArrayList<>(data.build());
  }

  public List<MessageEmbed.Field> analyzeMatchups(Lane lane) {
    final List<MessageEmbed.Field> fields = new ArrayList<>();
    fields.add(getPlayerHeadField(lane));
    final var data = new EmbedFieldBuilder<>(handleMatchups().stream().filter(championData -> championData.champion() != null)
        .sorted().toList())
        .add("Matchup", PlayerMatchupData::getChampionString)
        .add("Games", playerMatchupData -> String.valueOf(playerMatchupData.standing().getGames()))
        .add("Winrate", PlayerMatchupData::getWinrate);
    fields.addAll(data.build());
    return fields;
  }

  private MessageEmbed.Field getPlayerHeadField(Lane lane) {
    final Object[] data = player().analyze(ScoutingGameType.MATCHMADE, days).performance()
        .get("concat(avg(_performance.kills), ' / ', avg(_performance.deaths), ' / ', avg(_performance.assists))", String.class)
        .get("concat(round(avg(_performance.gold) * 100 / avg(_teamperf.total_gold), 1), ' %')", String.class)
        .get("concat(round(avg(_performance.damage) * 100 / avg(_teamperf.total_damage), 1), ' %')", String.class)
        .get("concat(round(avg(_performance.creeps), 0), '')", String.class)
        .get("concat(round(avg(_performance.vision), 0), '')", String.class)
        .single();
    final String csOrVision = (String) (lane.equals(Lane.UTILITY) ? data[4] : data[3]);
    return new MessageEmbed.Field(lane.getDisplayName() + ": " + player().getSummonerName() + "(" + getGames() + " Games - " +
        player().getRanks().getCurrent() + ")",
        "KDA: " + data[0] + " - Gold: " + data[1] + " - Damage: " + data[2] + " - CS/VS: " + csOrVision, false);
  }

  private List<PlayerMatchupData> handleMatchups() {
    final List<Object[]> matchupList = performance().get("enemy_champion", Integer.class).get("count(performance_id)", Integer.class).get("avg(_teamperf.win)", Double.class).groupBy(new SQLGroup("champion").having("count(performance_id) > 4")).ascending("avg(_teamperf.win)").list();
    return matchupList.stream().map(objs -> new PlayerMatchupData(new Query<>(Champion.class).entity(objs[0]),
        new Standing((int) (((Long) objs[1]).intValue() * ((BigDecimal) objs[2]).doubleValue()), (int) (((Long) objs[1]).intValue() * (1 - (((BigDecimal) objs[2]).doubleValue())))))).toList();
  }

  private List<PlayerAnalyzerData> handlePicks() {
    List<Object[]> presentPicks = selection().get("champion", Champion.class).get("count(selection_id)", Integer.class).groupBy("champion").descending("count(selection_id)").list();
    if (presentPicks.isEmpty()) {
      final Query<Performance> performanceQuery = new Query<>(Performance.class).distinct("_teamperf.game", Integer.class)
          .join(new JoinQuery<>(Performance.class, TeamPerf.class).col("t_perf")).join(new JoinQuery<>(TeamPerf.class, Game.class))
          .where("player", player()).and(Condition.Comparer.GREATER_EQUAL, "_game.start_time", getStart());
      presentPicks = new Query<>(Selection.class).get("champion", Champion.class).get("count(selection_id)", Integer.class)
          .join(new JoinQuery<>(new Query<>(" inner join (" + performanceQuery.getSelectString(true) + ") as s1 on _selection.game = s1.game", performanceQuery.getParameters())))
          .groupBy("champion").descending("count(selection_id)").list();
    }

    final List<Object[]> picksList = performance().get("champion", Champion.class).get("count(performance_id)", Integer.class).groupBy("champion").descending("count(performance_id)").list();
    final Map<Champion, Integer> pickMap = picksList.stream().collect(Collectors.toMap(objs -> new Query<>(Champion.class).entity(objs[0]), objs -> ((Long) objs[1]).intValue(), (a, b) -> b));

    final List<Object[]> mmList = player().analyze(ScoutingGameType.MATCHMADE, days).performance().get("champion", Champion.class).get("count(performance_id)", Integer.class).groupBy("champion").descending("count(performance_id)").list();
    final Map<Champion, Integer> mmMap = mmList.stream().collect(Collectors.toMap(objs -> new Query<>(Champion.class).entity(objs[0]), objs -> ((Long) objs[1]).intValue(), (a, b) -> b));
    final List<Object[]> winsList = player().analyze(ScoutingGameType.MATCHMADE, days).performance().get("champion", Champion.class).get("count(performance_id)", Integer.class).where("_teamperf.win", true).groupBy("champion").descending("count(performance_id)").list();
    final Map<Champion, Integer> winsMap = winsList.stream().collect(Collectors.toMap(objs -> new Query<>(Champion.class).entity(objs[0]), objs -> ((Long) objs[1]).intValue(), (a, b) -> b));
    final List<Object[]> games = performance().get("count(performance_id)", Integer.class).list();
    final Long amountOfGames = ((Long) games.get(0)[0]);
    final Map<Champion, PlayerAnalyzerData> data = new HashMap<>();
    for (final Object[] presentPick : presentPicks) {
      final Champion champion = new Query<>(Champion.class).entity(presentPick[0]);
      final int occurrences = ((Long) presentPick[1]).intValue();
      final int picks = pickMap.getOrDefault(champion, 0);
      final int mmGames = mmMap.getOrDefault(champion, 0);
      final int wins = winsMap.getOrDefault(champion, 0);
      if (picks > 0 || mmGames >= 10) {
        data.put(champion, new PlayerAnalyzerData(champion, occurrences * 1. / amountOfGames, picks, mmGames, wins));
      }
    }
    for (Object[] objs : picksList) {
      final Champion champion = new Query<>(Champion.class).entity(objs[0]);
      final int amount = ((Long) (objs[1])).intValue();
      if (!data.containsKey(champion) && amount >= 10) {
        data.put(champion, new PlayerAnalyzerData(champion, 0, 0, amount, winsMap.getOrDefault(champion, 0)));
      }
    }
    final List<PlayerAnalyzerData> outputList = data.values().stream().sorted(Comparator.reverseOrder()).toList();
    return outputList.subList(0, Math.min(8, outputList.size()));
  }

  public int getGames() {
    return ((Long) (performance().get("count(performance_id)", Integer.class).single()[0])).intValue();
  }

  public record PlayerAnalyzerData(Champion champion, double presence, int competitiveGames, int matchMadeGames, int matchMadeWins)
      implements Comparable<PlayerAnalyzerData> {
    public String getChampionString() {
      return champion.getName();
    }

    public String getCompetitiveString() {
      return Math.round(presence * 100) + "% - " + competitiveGames;
    }

    public String getMatchmadeString() {
      return matchMadeGames + " - " + Math.round(matchMadeWins * 100.0 / matchMadeGames) + "%";
    }

    @Override
    public int compareTo(@NotNull PlayerAnalyzerData o) {
      return Comparator.comparing(PlayerAnalyzerData::presence).compare(this, o);
    }
  }

  public record PlayerMatchupData(Champion champion, Standing standing) implements Comparable<PlayerMatchupData> {
    public String getChampionString() {
      return champion.getName();
    }

    public String getWinrate() {
      return standing.getWinrate().toString();
    }

    @Override
    public int compareTo(@NotNull PlayerMatchupData o) {
      return standing.compareTo(o.standing);
    }
  }
}
