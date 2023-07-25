package de.xahrie.trues.api.community.application;

import de.xahrie.trues.api.database.connector.Listing;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

@RequiredArgsConstructor
@Listing(Listing.ListingType.CAPITALIZE)
public enum TeamRole {
  /**
   * hilft für einen Tag aus
   */
  STANDIN("Standin", "Ersatz erhält für 1 Tag Zugriff"),
  /**
   * Bewerbungsgespräch angenommen <br>
   * Wenn Tryout für Team dann ist dies die Auswahlrolle
   */
  TRYOUT("Tryout", "Tryout erhält für 14 Tage Zugriff"),
  /**
   * Substitude oder wenn temporär Tryout für dieses Team
   */
  SUBSTITUTE("Substitute", "Spieler erhält permanenten Zugriff"),
  /**
   * Stammspieler (max. 5 pro Team)
   */
  MAIN("Mainspieler", "Spieler erhält permanenten Zugriff"),
  ORGA_TRYOUT(null, null),
  /**
   * Teil als Staffmember
   */
  ORGA(null, null),
  REMOVE("entfernen", "Spieler wird aus dem Team entfernt");

  private final String name;
  private final String description;

  public String getName() {
    return name == null ? name() : name;
  }

  public SelectOption toSelectOption() {
    return SelectOption.of(name, name()).withDescription(description);
  }

  @Override
  public String toString() {
    return name;
  }
}
