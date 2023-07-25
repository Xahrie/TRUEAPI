package de.xahrie.trues.api.riot.champion;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
@ToString
@Table("champion_mastery")
public class ChampionMastery implements Entity<ChampionMastery> {
  @Serial
  private static final long serialVersionUID = 6171714758229050668L;

  @Setter
  private int id; // champion_mastery_id
  private final int playerId; // player
  private final int championId; // champion
  private final int points; // mastery_points
  private final byte level; // mastery_level
  private final LocalDateTime lastPlayed; // last_time_played

  private Player player;
  public Player getPlayer() {
    if (player == null) this.player = new Query<>(Player.class).entity(playerId);
    return player;
  }

  private Champion champion;
  public Champion getChampion() {
    if (champion == null) this.champion = new Query<>(Champion.class).entity(championId);
    return champion;
  }

  public ChampionMastery(@NotNull Player player, int championId, int points, byte level, LocalDateTime lastPlayed) {
    this.player = player;
    this.playerId = player.getId();
    this.championId = championId;
    this.points = points;
    this.level = level;
    this.lastPlayed = lastPlayed;
  }

  private ChampionMastery(int id, int playerId, int championId, int points, byte level, LocalDateTime lastPlayed) {
    this.id = id;
    this.playerId = playerId;
    this.championId = championId;
    this.points = points;
    this.level = level;
    this.lastPlayed = lastPlayed;
  }

  public static ChampionMastery get(List<Object> objects) {
    return new ChampionMastery(
        (int) objects.get(0),
        (int) objects.get(1),
        (int) objects.get(2),
        (int) objects.get(3),
        (byte) objects.get(4),
        (LocalDateTime) objects.get(5)
    );
  }

  @Override
  public ChampionMastery create() {
    return new Query<>(ChampionMastery.class).key("player", playerId).key("champion", championId)
        .col("mastery_points", points)
        .col("mastery_level", level)
        .col("last_time_played", lastPlayed)
        .insert(this);
  }
}