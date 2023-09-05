package de.xahrie.trues.api.community.betting;

import de.xahrie.trues.api.database.connector.Listing;

@Listing(Listing.ListingType.ORDINAL)
public enum BetStatus {
  NONE,
  WORLDS,
  INTERN,
  BOTH
}
