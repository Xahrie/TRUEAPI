package de.xahrie.trues.api.riot.game;

import de.xahrie.trues.api.database.connector.Listing;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import no.stelar7.api.r4j.basic.constants.types.lol.GameQueueType;

@RequiredArgsConstructor
@Getter
@Listing(Listing.ListingType.ORDINAL)
public enum GameType {
  TOURNAMENT(0),
  CUSTOM(0),
  CLASH(700),
  RANKED_FLEX(440),
  RANKED_SOLO(400),
  NORMAL_DRAFT(420),
  NORMAL_BLIND(430);

  private final int id;

  public static GameType fromQueueType(GameQueueType queueType) {
    return switch (queueType) {
      case CLASH -> CLASH;
      case RANKED_FLEX_SR -> RANKED_FLEX;
      case TEAM_BUILDER_RANKED_SOLO -> RANKED_SOLO;
      case TEAM_BUILDER_DRAFT_UNRANKED_5X5 -> NORMAL_DRAFT;
      case NORMAL_5V5_BLIND_PICK -> NORMAL_BLIND;
      case CUSTOM -> throw new IllegalArgumentException("Wird woanders verarbeitet!");
      default -> throw new IllegalArgumentException("Keine Queue bekannt! " + queueType.name());
    };
  }
}
