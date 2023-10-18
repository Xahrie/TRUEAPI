package de.xahrie.trues.api.coverage.participator.model;

import java.io.Serial;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.database.query.SQLEnum;
import de.xahrie.trues.api.riot.performance.Lane;
import de.xahrie.trues.api.util.Util;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

/**
 * Lineup wird submitted ({@code ordered} = Command | {@code not ordered} = matchlog)
 */
@Getter
@Setter
@Table("coverage_lineup")
public class Lineup implements Entity<Lineup>, Comparable<Lineup> {
  @Serial
  private static final long serialVersionUID = 3196905801592447600L;

  private int id;
  private final int participatorId, playerId; // coverage_team, player
  private Lane lane; // lineup_id

  public void setLane(@NotNull Lane lane) {
    if (this.lane == lane) return;
    this.lane = lane;
    new Query<>(Lineup.class).col("lineup_id", lane).update(id);
  }

  public Lineup(@NotNull Participator participator, @NotNull Player player, @NotNull Lane lane) {
    this.participator = participator;
    this.participatorId = participator.getId();
    this.player = player;
    this.playerId = Util.avoidNull(player, 0, Player::getId);
    this.lane = lane;
  }

  private Lineup(int id, int participatorId, int playerId, Lane lane) {
    this.id = id;
    this.participatorId = participatorId;
    this.playerId = playerId;
    this.lane = lane;
  }

  public static Lineup get(List<Object> objects) {
    return new Lineup(
        (int) objects.get(0),
        (int) objects.get(1),
        (int) objects.get(2),
        new SQLEnum<>(Lane.class).of(objects.get(3))
    );
  }

  @Override
  public Lineup create() {
    final Lineup lineup = new Query<>(Lineup.class).key("coverage_team", participatorId).key("player", playerId)
        .col("lineup_id", lane).insert(this);
    return getParticipator().getTeamLineup().add(lineup);
  }

  @Override
  public void delete() {
    getParticipator().getTeamLineup().remove(this);
    Entity.super.delete();
  }

  @Override
  public int compareTo(@NotNull Lineup o) {
    return Comparator.comparing(Lineup::getParticipator).thenComparing(Lineup::getLane).compare(this, o);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof final Lineup lineup)) return false;
    if (getId() != 0) return getId() == lineup.getId();
    return Objects.equals(getParticipator(), lineup.getParticipator()) && getLane() == lineup.getLane()
        && Objects.equals(getPlayer(), lineup.getPlayer());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getParticipator(), getLane(), getPlayer());
  }

  private Participator participator;

  public Participator getParticipator() {
    if (participator == null) this.participator = new Query<>(Participator.class).entity(participatorId);
    return participator;
  }

  private Player player;

  public Player getPlayer() {
    if (player == null) this.player = new Query<>(Player.class).entity(playerId);
    return player;
  }
}
