package de.xahrie.trues.api.coverage.player.model;

import java.util.Objects;

import com.merakianalytics.orianna.types.common.Tier;
import de.xahrie.trues.api.database.connector.Listing;
import de.xahrie.trues.api.util.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;

@Log
public record Rank(RankTier tier, Division division, short points) implements Comparable<Rank> {

  public static Rank fromMMR(int mmr) {
    final int tierIndex = mmr / 400;
    final RankTier rankTier = tierIndex > 9 ? RankTier.CHALLENGER : RankTier.values()[tierIndex];

    final int divisionIndex = 4 - ((mmr - (tierIndex * 400)) / 100);
    final Division division = rankTier.ordinal() >= RankTier.MASTER.ordinal() ? Division.I : Division.values()[divisionIndex];
    final short points = (short) ((mmr >= RankTier.MASTER.ordinal() * 400) ? (mmr - RankTier.MASTER.ordinal() * 400) : mmr % 100);
    return new Rank(rankTier, division, points);
  }

  @Override
  public String toString() {
    if (tier.equals(RankTier.UNRANKED)) return "Unranked";
    final String divisionString = tier.ordinal() < RankTier.MASTER.ordinal() ? " " + division : "";
    return tier + divisionString + " " + points + " LP";
  }

  public int getMMR() {
    return (tier.ordinal() >= RankTier.MASTER.ordinal() ? RankTier.MASTER.ordinal() * 400 : tier.ordinal() * 400 + division.getPoints())
        + points;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final Rank rank = (Rank) o;
    return points == rank.points && tier == rank.tier && division == rank.division;
  }

  public boolean like(Rank rank) {
    if (this == rank) return true;
    if (rank == null) return false;
    return tier == rank.tier && division == rank.division;
  }

  public boolean like(RankTier tier, Division division) {
    return this.tier == tier && this.division == division;
  }

  @Override
  public int hashCode() {
    return Objects.hash(tier, division, points);
  }

  @Override
  public int compareTo(@NotNull Rank o) {
    return Integer.compare(o.getMMR(), getMMR());
  }

  @Listing(Listing.ListingType.ORDINAL)
  public enum RankTier implements Comparable<RankTier> {
    UNRANKED, IRON, BRONZE, SILVER, GOLD, PLATINUM, EMERALD, DIAMOND, MASTER, GRANDMASTER, CHALLENGER;

    @Override
    public String toString() {
      return StringUtils.capitalizeFirst(name());
    }
  }

  @Listing(Listing.ListingType.ORDINAL)
  @RequiredArgsConstructor
  @Getter
  public enum Division {
    ZERO(0),
    I(300),
    II(200),
    III(100),
    IV(0);
    private final int points;

    @Override
    public String toString() {
      return equals(ZERO) ? "" : name();
    }
  }
}
