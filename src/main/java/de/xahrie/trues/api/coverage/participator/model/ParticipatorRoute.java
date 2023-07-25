package de.xahrie.trues.api.coverage.participator.model;

import de.xahrie.trues.api.coverage.league.model.AbstractLeague;
import de.xahrie.trues.api.database.connector.Listing;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ParticipatorRoute {
  private AbstractLeague league; // route_group
  private RouteType type; // route_type
  private Short value; // route_value

  @Override
  public String toString() {
    if (type == null) return "n.A.";
    return switch (type) {
      case LOSER -> "Verlierer M" + value;
      case PLACE -> value + ". " + league.getName();
      case SEEDED -> "Seed " + value;
      case WINNER -> "Sieger M" + value;
    };
  }

  @Listing(Listing.ListingType.LOWER)
  public enum RouteType {
    LOSER,
    PLACE,
    SEEDED,
    WINNER
  }
}
