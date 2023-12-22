package de.xahrie.trues.api.coverage.team;

import org.jetbrains.annotations.Nullable;

public record TeamHistory(Integer calibrationResult, Integer group, Integer groupResult, Integer playoff,
                          Boolean hasWon) {
  public boolean isValid() {
    return group != null;
  }

  @Nullable
  public Boolean isPromotedOrDemotedViaPlayoffs() {
    if (hasWon() == null || group() == 9) return hasWon();
    if (group().equals(playoff) && !hasWon) return false;
    if ((group() + 1) == playoff && hasWon) return true;
    return null;
  }
}
