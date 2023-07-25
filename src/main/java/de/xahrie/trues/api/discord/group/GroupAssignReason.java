package de.xahrie.trues.api.discord.group;

import de.xahrie.trues.api.database.connector.Listing;

@Listing(Listing.ListingType.UPPER)
public enum GroupAssignReason {
  ADD,
  BDAY,
  RANKUP
}
