package de.xahrie.trues.api.riot;

import de.xahrie.trues.api.database.connector.Listing;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Listing(Listing.ListingType.LOWER)
@Getter
@RequiredArgsConstructor
public enum GameMap {
  SUMMONERS_RIFT("SR", "Summoners Rift"),
  TWISTED_TREELINE("TT", "Twisted Treeline"),
  THE_CRYSTAL_SCAR("Dominion", "The Crystal Scar"),
  HOWLING_ABYSS("HA", "Howling Abyss"),
  COSMIC_RUINS("Dark Star", "Cosmic Ruins"),
  VALORAN_CITY_PARK("Valoran", "Valoran City Park"),
  SUBSTRUCTURE_43("Project", "Substructure 43"),
  CRASH_SITE("Odysee", "Crash Site"),
  CONVERGENCE("TFT", "Convergence"),
  TEMPLE_OF_LOTUS_AND_LILLY("Nexus", "Temple of Lotus and Lilly");

  private final String abbreviation;
  private final String name;
}