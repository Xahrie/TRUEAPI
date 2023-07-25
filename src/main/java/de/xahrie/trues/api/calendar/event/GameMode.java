package de.xahrie.trues.api.calendar.event;

import de.xahrie.trues.api.database.connector.Listing;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@Listing(Listing.ListingType.UPPER)
public enum GameMode {
  SAUFEN("Aram saufen", null),
  HANDICAP("Handicap League", null);

  public final String name;
  public final String rules;

  @Override
  public String toString() {
    return name;
  }

  public String getRules() {
    return rules == null ? "**Regeln werden später erklärt.**" : "__Regeln:__\n" + rules;
  }
}
