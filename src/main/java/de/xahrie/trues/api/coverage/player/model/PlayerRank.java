package de.xahrie.trues.api.coverage.player.model;

import de.xahrie.trues.api.coverage.season.Season;
import de.xahrie.trues.api.coverage.season.SeasonFactory;
import de.xahrie.trues.api.coverage.team.model.Standing;
import de.xahrie.trues.api.database.connector.SQLUtils;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.database.query.SQLEnum;
import de.xahrie.trues.api.util.Format;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.List;

@Getter
@Setter
@Table("player_ranked")
@ExtensionMethod(SQLUtils.class)
public class PlayerRank implements Entity<PlayerRank>, Comparable<PlayerRank> {
  @Serial
  private static final long serialVersionUID = 4008920298892200060L;

  @NotNull
  @Contract("_ -> new")
  public static Rank fromMMR(int mmr) {
    return Rank.fromMMR(mmr);
  }

  private int id; // player_ranked_id
  private final int seasonId; // season
  private final int playerId; // player
  private Rank rank;
  private int wins; // wins
  private int losses; // losses

  private Season season;

  public Season getSeason() {
    if (season == null) this.season = new Query<>(Season.class).entity(seasonId);
    return season;
  }

  private Player player;

  public Player getPlayer() {
    if (player == null) this.player = new Query<>(Player.class).entity(playerId);
    return player;
  }

  public Standing getWinrate() {
    return new Standing(wins, losses);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof PlayerRank && this.playerId == ((PlayerRank) obj).getPlayerId();
  }

  @Override
  public String toString() {
    return rank.toString() + (rank.tier().equals(Rank.RankTier.UNRANKED) ? "" : " - (" + getWinrate().format(Format.ADDITIONAL) + ")");
  }

  public PlayerRank(Player player, @NotNull Rank rank, int wins, int losses) {
    this(player, rank.tier(), rank.division(), rank.points(), wins, losses);
  }

  public PlayerRank(@NotNull Player player, Rank.RankTier tier, Rank.Division division, short points, int wins, int losses) {
    this.season = SeasonFactory.getCurrentSeason();
    this.seasonId = season.getId();
    this.playerId = player.getId();
    this.player = player;
    this.rank = new Rank(tier, division, points);
    this.wins = wins;
    this.losses = losses;
  }

  private PlayerRank(int id, int seasonId, int playerId, Rank rank, int wins, int losses) {
    this.id = id;
    this.seasonId = seasonId;
    this.playerId = playerId;
    this.rank = rank;
    this.wins = wins;
    this.losses = losses;
  }

  public static PlayerRank get(List<Object> objects) {
    return new PlayerRank(
        (int) objects.get(0),
        (int) objects.get(1),
        (int) objects.get(2),
        new Rank(new SQLEnum<>(Rank.RankTier.class).of(objects.get(3)), new SQLEnum<>(Rank.Division.class).of(objects.get(4)), objects.get(5).shortValue()),
        (int) objects.get(6),
        (int) objects.get(7)
    );
  }

  @Override
  public PlayerRank create() {
    return new Query<>(PlayerRank.class).key("season", seasonId).key("player", playerId)
        .col("tier", rank.tier()).col("division", rank.division()).col("points", rank.points())
        .col("wins", wins).col("losses", losses).insert(this);
  }

  @Override
  public int compareTo(@NotNull PlayerRank o) {
    return -season.compareTo(o.getSeason());
  }
}
