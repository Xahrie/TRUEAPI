package de.xahrie.trues.api.coverage.player.model;

import java.io.Serial;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(value = "player", department = "other")
public class PlayerImpl extends Player implements Entity<PlayerImpl> {
  @Serial
  private static final long serialVersionUID = 2925841006082764104L;

  public PlayerImpl(String summonerName, String summonerId, String puuid) {
    super(summonerName, puuid, summonerId);
    this.updated = LocalDateTime.of(1, Month.JANUARY, 1, 0, 0);
  }

  private PlayerImpl(int id, String puuid, String summonerId, String summonerName, Integer discordUserId, Integer teamId,
                     LocalDateTime updated, boolean played) {
    super(id, puuid, summonerId, summonerName, discordUserId, teamId, updated, played);
  }

  public static PlayerImpl get(List<Object> objects) {
    return new PlayerImpl(
        (int) objects.get(0),
        (String) objects.get(2),
        (String) objects.get(3),
        (String) objects.get(4),
        (Integer) objects.get(5),
        (Integer) objects.get(6),
        (LocalDateTime) objects.get(7),
        (boolean) objects.get(8));
  }

  @Override
  public PlayerImpl create() {
    return new Query<>(PlayerImpl.class).key("lol_puuid", puuid)
        .col("lol_summoner", summonerId).col("lol_name", summonerName).col("discord_user", discordUserId).col("team", teamId)
        .col("updated", updated).col("played", played)
        .insert(this);
  }

}
