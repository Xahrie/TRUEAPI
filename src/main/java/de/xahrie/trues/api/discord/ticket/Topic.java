package de.xahrie.trues.api.discord.ticket;

import java.util.Arrays;

import de.xahrie.trues.api.database.connector.Listing;
import de.xahrie.trues.api.discord.group.DiscordGroup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

@Listing(Listing.ListingType.LOWER)
@RequiredArgsConstructor
@Getter
public enum Topic {
  BUG_REPORT("Bug melden", DiscordGroup.STAFF),
  FEATURE_REQUEST("Featurerequest", DiscordGroup.STAFF),
  USER_REPORT("Nutzerreport", DiscordGroup.ADMIN);

  private final String name;
  private final DiscordGroup visibility;

  @Nullable
  public static Topic fromName(String name) {
    return Arrays.stream(Topic.values()).filter(topic -> topic.getName().equals(name)).findFirst().orElse(null);
  }
}
