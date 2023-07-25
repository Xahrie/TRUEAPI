package de.xahrie.trues.api.scouting;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.collections.SortedList;
import de.xahrie.trues.api.riot.game.Selection;
import de.xahrie.trues.api.riot.performance.Lane;
import de.xahrie.trues.api.riot.performance.Performance;
import de.xahrie.trues.api.util.StringUtils;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(StringUtils.class)
public abstract class AnalyzeManager {
  private static Map<PlayerLane, LaneGames> laneExperience = new ConcurrentHashMap<>();

  public static void reset() {
    laneExperience = new HashMap<>();
  }

  public static void delete(Player player) {
    new HashSet<>(laneExperience.keySet()).stream().filter(lane -> lane.player().equals(player)).forEach(lane -> laneExperience.remove(lane));
  }

  protected static int get(Lane lane, Player player, ScoutingGameType gameType, int days) {
    final PlayerLane playerLane = new PlayerLane(player, lane, gameType, days);
    final LaneGames laneGames = laneExperience.get(playerLane);
    return laneGames == null || laneGames.inserted().isBefore(LocalDateTime.now().minusDays(1)) ? create(playerLane) : laneGames.amount();
  }

  private static int create(PlayerLane playerLane) {
    int lookingFor = 0;
    final List<Object[]> list = playerLane.analyze().performance().get("lane", Lane.class).get("count(performance_id)", Integer.class).groupBy("lane").descending("count(`performance_id`)").list();
    for (Object[] o : list) {
      if (!(o[0] instanceof Lane lane) || lane.equals(Lane.UNKNOWN)) continue;

      final int amount = ((Long) o[1]).intValue();
      final PlayerLane pl = new PlayerLane(playerLane.player(), lane, playerLane.gameType(), playerLane.days());
      laneExperience.put(pl, new LaneGames(amount, LocalDateTime.now()));
      if (lane.equals(playerLane.lane())) lookingFor = amount;
    }
    return lookingFor;
  }

  protected final AbstractTeam team;
  protected final List<Player> players;
  protected final ScoutingGameType gameType;
  protected final int days;

  public AnalyzeManager(AbstractTeam team, List<Player> players) {
    this(team, SortedList.of(players), ScoutingGameType.TEAM_GAMES, 180);
  }

  public AnalyzeManager(AbstractTeam team, List<Player> players, ScoutingGameType gameType, int days) {
    this.team = team;
    this.players = SortedList.of(players);
    this.gameType = gameType;
    this.days = days;
  }

  public abstract Query<Performance> performance();
  public abstract Query<Selection> selection();
  protected abstract Query<Performance> gameTypeString(Query<Performance> query);

  public LocalDateTime getStart() {
    return LocalDateTime.now().minusDays(days);
  }

  public AnalyzeManager with(List<Player> players) {
    this.players.addAll(players);
    return this;
  }

  public record PlayerLane(Player player, Lane lane, ScoutingGameType gameType, int days) {
    public PlayerAnalyzer analyze() {
      return player.analyze(gameType, days);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PlayerLane that)) return false;
      return days == that.days && player.equals(that.player) && lane == that.lane && gameType == that.gameType;
    }

    @Override
    public int hashCode() {
      return Objects.hash(player, lane, gameType, days);
    }
  }

  public record LaneGames(Integer amount, LocalDateTime inserted) {
  }
}
