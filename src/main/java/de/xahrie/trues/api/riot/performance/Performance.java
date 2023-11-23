package de.xahrie.trues.api.riot.performance;

import java.io.Serial;
import java.util.Comparator;
import java.util.List;

import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.database.connector.SQLUtils;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.database.query.SQLEnum;
import de.xahrie.trues.api.riot.KDA;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;

@Getter
@Table("performance")
@ExtensionMethod(SQLUtils.class)
public class Performance implements Entity<Performance>, Comparable<Performance> {
  @Serial
  private static final long serialVersionUID = -8274031327889064909L;

  @Setter
  private int id; // perf_id
  private final int teamPerformanceId; // t_perf
  private final Integer playerId; // player
  private final Lane lane; // lane
  private final Matchup matchup; // champion, enemy_champion
  private final KDA kda;
  private final int gold; // gold
  private final Integer damage; // damage
  private final Integer vision; // vision
  private final int creeps; // creeps

  private TeamPerf teamPerformance; // t_perf

  public TeamPerf getTeamPerformance() {
    if (teamPerformance == null) this.teamPerformance = new Query<>(TeamPerf.class).entity(teamPerformanceId);
    return teamPerformance;
  }

  private Player player; // player

  public Player getPlayer() {
    if (player == null) this.player = new Query<>(Player.class).entity(playerId);
    return player;
  }

  public Performance(TeamPerf teamPerformance, Player player, Lane lane, Matchup matchup, KDA kda, int gold, Integer damage, Integer vision, int creeps) {
    this.teamPerformance = teamPerformance;
    this.teamPerformanceId = teamPerformance.getId();
    this.player = player;
    this.playerId = player.getId();
    this.lane = lane;
    this.matchup = matchup;
    this.kda = kda;
    this.gold = gold;
    this.damage = damage;
    this.vision = vision;
    this.creeps = creeps;
  }

  private Performance(int id, int teamPerformanceId, Integer playerId, Lane lane, Matchup matchup, KDA kda, int gold, Integer damage, Integer vision, int creeps) {
    this.id = id;
    this.teamPerformanceId = teamPerformanceId;
    this.playerId = playerId;
    this.lane = lane;
    this.matchup = matchup;
    this.kda = kda;
    this.gold = gold;
    this.damage = damage;
    this.vision = vision;
    this.creeps = creeps;
  }

  public static Performance get(List<Object> objects) {
    return new Performance(
        objects.get(0).intValue(),
        objects.get(1).intValue(),
        objects.get(2).intValue(),
        new SQLEnum<>(Lane.class).of(objects.get(3)),
        new Matchup((Integer) objects.get(4), (Integer) objects.get(5)),
        new KDA(objects.get(6).shortValue(), objects.get(7).shortValue(), objects.get(8).shortValue()),
        (int) objects.get(9),
        (Integer) objects.get(10),
        (Integer) objects.get(11),
        (int) objects.get(12));
  }

  @Override
  public Performance create() {
    return new Query<>(Performance.class).key("t_perf", teamPerformanceId).key("player", playerId).key("lane", lane)
        .col("champion", matchup.getChampion()).col("enemy_champion", matchup.getOpposingChampion()).col("kills", kda.kills()).col("deaths", kda.deaths())
        .col("assists", kda.assists()).col("gold", gold).col("damage", damage).col("vision", vision).col("creeps", creeps)
        .insert(this, getTeamPerformance()::addPerformance);
  }

  public String getPlayername() {
    return getPlayer().getName().toString();
  }

  public String getStats() {
    return kda.toString() + "(" + (lane.equals(Lane.UTILITY) ? vision : creeps) + ")";
  }

  @Override
  public int compareTo(@NotNull Performance o) {
    return Comparator.comparing(Performance::getTeamPerformance).thenComparing(Performance::getLane).compare(this, o);
  }
}
