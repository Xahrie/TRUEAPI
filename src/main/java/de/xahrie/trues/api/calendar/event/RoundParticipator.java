package de.xahrie.trues.api.calendar.event;

import java.io.Serial;
import java.util.List;
import java.util.Objects;

import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import lombok.Getter;
import lombok.Setter;

@Getter
@Table(value = "event_participator")
public class RoundParticipator implements Entity<RoundParticipator> {
  @Serial
  private static final long serialVersionUID = 3381142629939377992L;

  @Setter
  private int id;
  private final int roundId;
  private Round round;

  public Round getRound() {
    if (round == null) this.round = new Query<>(Round.class).entity(roundId);
    return round;
  }

  private final int playerId;
  private Player player;

  public Player getPlayer() {
    if (player == null) this.player = new Query<>(Player.class).entity(playerId);
    return player;
  }

  private Integer teamIndex;

  public Integer getTeamIndex() {
    if (teamIndex == null) this.teamIndex = new Query<>(RoundParticipator.class).entity(id).teamIndex;
    return teamIndex;
  }

  public RoundParticipator(Round round, Player player) {
    this.round = round;
    this.roundId = round.getId();
    this.player = player;
    this.playerId = player.getId();
  }

  private RoundParticipator(int id, int roundId, int playerId, Integer teamIndex) {
    this.id = id;
    this.roundId = roundId;
    this.playerId = playerId;
    this.teamIndex = teamIndex;
  }

  public static RoundParticipator get(List<Object> objects) {
    return new RoundParticipator(
        (int) objects.get(0),
        (int) objects.get(1),
        (int) objects.get(2),
        (Integer) objects.get(3)
    );
  }

  @Override
  public RoundParticipator create() {
    return new Query<>(RoundParticipator.class).key("event_round", round).key("player", player)
        .col("team_index", getTeamIndex()).insert(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof final RoundParticipator that)) return false;
    return getRoundId() == that.getRoundId() && getPlayerId() == that.getPlayerId();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getRoundId(), getPlayerId());
  }
}
