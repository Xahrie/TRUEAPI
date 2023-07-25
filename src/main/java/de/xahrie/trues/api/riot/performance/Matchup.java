package de.xahrie.trues.api.riot.performance;

import java.util.Objects;

import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.riot.champion.Champion;
import de.xahrie.trues.api.util.Util;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@RequiredArgsConstructor
public final class Matchup {
  private final int championId;
  private final Integer opposingChampionId;

  public Matchup(@NotNull Champion champion, @Nullable Champion opposingChampion) {
    this.champion = champion;
    this.championId = champion.getId();
    this.opposingChampion = opposingChampion;
    this.opposingChampionId = Util.avoidNull(opposingChampion, Champion::getId);
  }

  @Override
  public String toString() {
    return Util.avoidNull(getChampion(), "no data", Champion::getName) + " vs " +
        Util.avoidNull(getOpposingChampion(), "no data", Champion::getName);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof final Matchup matchup)) return false;
    return championId == matchup.getChampionId() && Objects.equals(opposingChampionId, matchup.getOpposingChampionId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getChampionId(), getOpposingChampionId());
  }

  private Champion champion;
  public Champion getChampion() {
    if (champion == null) this.champion = new Query<>(Champion.class).entity(championId);
    return champion;
  }

  private Champion opposingChampion;
  public Champion getOpposingChampion() {
    if (opposingChampion == null) this.opposingChampion = new Query<>(Champion.class).entity(opposingChampionId);
    return opposingChampion;
  }
}
