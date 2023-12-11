package de.xahrie.trues.api.coverage.player.model;

import java.util.List;
import java.util.Objects;

import de.xahrie.trues.api.coverage.player.PrimePlayerFactory;
import de.xahrie.trues.api.database.connector.SQLUtils;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Condition;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.riot.api.RiotName;
import de.xahrie.trues.api.util.Util;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Getter
@Table("coverage_experience")
public class PlayerExperience implements Entity<PlayerExperience> {
  @Setter private int id;
  private final int prmUserId; // prm_id
  private int matches; // matches
  private int games; // games
  private Integer playerId; // player

  @NotNull
  public static PlayerExperience of(int prmUserId) {
    PlayerExperience experience = new Query<>(PlayerExperience.class).where("prm_id", prmUserId).entity();
    if (experience == null) {
      experience = new PlayerExperience(prmUserId).create();
      experience.setPlayer(new Query<>(PRMPlayer.class).where("prm_id", prmUserId).entity());
    }
    return experience;
  }

  @NotNull
  public static PlayerExperience of(int prmUserId, RiotName name) {
    final PlayerExperience experience = new PlayerExperience(prmUserId).create();
    experience.setPlayer(PrimePlayerFactory.getPrimePlayer(prmUserId, name));
    return experience;
  }

  private PlayerExperience(int prmUserId) {
    this.prmUserId = prmUserId;
    this.matches = 0;
    this.games = 0;
    this.playerId = null;
    this.player = null;
  }

  private PlayerExperience(int id, int prmUserId, int matches, int games, Integer playerId) {
    this.id = id;
    this.prmUserId = prmUserId;
    this.matches = matches;
    this.games = games;
    this.playerId = playerId;
  }

  @NotNull
  @Contract("_ -> new")
  public static PlayerExperience get(@NotNull List<Object> objects) {
    return new PlayerExperience(
        (int) objects.get(0),
        (int) objects.get(1),
        (int) objects.get(2),
        (int) objects.get(3),
        SQLUtils.intValue(objects.get(4))
    );
  }

  @Override
  public PlayerExperience create() {
    return new Query<>(PlayerExperience.class).key("prm_id", prmUserId).insert(this);
  }

  public PlayerExperience addGames(int amount) {
    if (amount == 0) return this;

    this.matches++;
    this.games += amount;
    new Query<>(PlayerExperience.class).col("matches", matches).col("games", games).update(id);
    return this;
  }

  private PRMPlayer player;

  public PRMPlayer getPlayer() {
    if (player == null && playerId != null)
      this.player = new Query<>(PRMPlayer.class).entity(playerId);
    return player;
  }

  public void setPlayer(PRMPlayer player) {
    if (!Objects.equals(getPlayer(), player)) return;
    this.player = player;
    this.playerId = Util.avoidNull(player, Player::getId);

    if (playerId != null)
      new Query<>(PRMPlayer.class).where("prm_id", prmUserId)
          .and(Condition.Comparer.NOT_EQUAL, "player_id", playerId).entityList()
          .forEach(player1 -> player1.setPrmUserId(null));

    new Query<>(PlayerExperience.class).col("player", playerId).update(id);
    if (player != null)
      player.setPrmUserId(prmUserId);
  }
}
