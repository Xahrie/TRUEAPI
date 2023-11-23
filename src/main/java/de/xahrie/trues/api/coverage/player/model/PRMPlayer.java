package de.xahrie.trues.api.coverage.player.model;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

import de.xahrie.trues.api.coverage.player.PlayerHandler;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.riot.api.RiotName;
import lombok.Getter;

@Getter
@Table(value = "player", department = "prime")
public class PRMPlayer extends Player implements Entity<PRMPlayer> {
  @Serial
  private static final long serialVersionUID = 1620593763353601777L;

  private Integer prmUserId; // prm_id

  public PRMPlayer(RiotName name, String puuid, String summonerId, Integer prmUserId) {
    super(name, puuid, summonerId);
    this.prmUserId = prmUserId;
  }

  private PRMPlayer(int id, String puuid, String summonerId, RiotName name, Integer discordUserId, Integer teamId,
                    LocalDateTime updated, boolean played, Integer prmUserId) {
    super(id, puuid, summonerId, name, discordUserId, teamId, updated, played);
    this.prmUserId = prmUserId;
  }

  public static PRMPlayer get(List<Object> objects) {
    return new PRMPlayer(
        (int) objects.get(0),
        (String) objects.get(2),
        (String) objects.get(3),
        RiotName.of((String) objects.get(4), (String) objects.get(5)),
        (Integer) objects.get(6),
        (Integer) objects.get(7),
        (LocalDateTime) objects.get(8),
        (boolean) objects.get(9),
        (Integer) objects.get(10));
  }

  @Override
  public PRMPlayer create() {
    boolean notExisting = (prmUserId != null && new Query<>(PRMPlayer.class).where("prm_id", prmUserId).entity(List.of()) == null);
    final PRMPlayer player = new Query<>(PRMPlayer.class).key("lol_puuid", puuid)
        .col("lol_summoner", summonerId).col("lol_name", name.getName()).col("lol_tag", name.getTag())
        .col("discord_user", discordUserId).col("team", teamId).col("updated", updated).col("played", played)
        .col("prm_id", prmUserId)
        .insert(this);
    if (notExisting) new PlayerHandler(null, this).updateElo();
    return player;
  }

  public void setPrmUserId(Integer prmUserId) {
    this.prmUserId = prmUserId;
    new Query<>(PRMPlayer.class).col("department", prmUserId == null ? "league" : "prime").col("prm_id", prmUserId).update(id);
  }

  @Override
  public boolean equals(Object other) {
    return super.equals(other) || (other instanceof Player && this.getId() == ((Player) other).getId());
  }
}
