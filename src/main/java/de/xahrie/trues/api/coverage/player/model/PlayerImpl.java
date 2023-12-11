package de.xahrie.trues.api.coverage.player.model;

import java.io.Serial;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.riot.api.RiotName;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@Table(value = "player", department = "other")
public class PlayerImpl extends Player implements Entity<PlayerImpl> {
  @Serial
  private static final long serialVersionUID = 2925841006082764104L;

  public PlayerImpl(RiotName name, String summonerId, String puuid) {
    super(name, puuid, summonerId);
    this.updated = LocalDateTime.of(1, Month.JANUARY, 1, 0, 0);
  }

  private PlayerImpl(int id, String puuid, String summonerId, RiotName name, Integer discordUserId, Integer teamId,
                     LocalDateTime updated, boolean played) {
    super(id, puuid, summonerId, name, discordUserId, teamId, updated, played);
  }

  @NotNull
  @Contract("_ -> new")
  public static PlayerImpl get(@NotNull List<Object> objects) {
    return new PlayerImpl(
        (int) objects.get(0),
        (String) objects.get(2),
        (String) objects.get(3),
        RiotName.of((String) objects.get(4), (String) objects.get(5)),
        (Integer) objects.get(6),
        (Integer) objects.get(7),
        (LocalDateTime) objects.get(8),
        (boolean) objects.get(9));
  }

  @Override
  public PlayerImpl create() {
    return new Query<>(PlayerImpl.class).key("lol_puuid", puuid)
        .col("lol_summoner", summonerId).col("lol_name", name.getName()).col("lol_tag", name.getTag())
        .col("discord_user", discordUserId).col("team", teamId).col("updated", updated).col("played", played)
        .insert(this);
  }

  public void setPRMId() {

  }

}
