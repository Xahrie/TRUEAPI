package de.xahrie.trues.api.community.application;

import java.util.Arrays;

import de.xahrie.trues.api.database.connector.Listing;
import de.xahrie.trues.api.discord.group.DiscordGroup;
import de.xahrie.trues.api.discord.message.Emote;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
@Getter
@Listing(Listing.ListingType.CAPITALIZE)
public enum TeamPosition {
  TOP(DiscordGroup.TOP, "Toplaner", null, Emote.TOP),
  JUNGLE(DiscordGroup.JUNGLE, "Jungler", null, Emote.JUNGLE),
  MIDDLE(DiscordGroup.MIDDLE, "Midlaner", null, Emote.MIDDLE),
  BOTTOM(DiscordGroup.BOTTOM, "Botlaner", null, Emote.BOTTOM),
  SUPPORT(DiscordGroup.SUPPORT, "Supporter", null, Emote.SUPPORT),
  TEAM_COACH(DiscordGroup.TEAM_COACH, "Team Coach/Mentor", "Begleiter für Teams", null),
  COACH(DiscordGroup.TEAM_COACH, "Coach", "Genereller Coach", null),
  EVENT_PLANNING(DiscordGroup.EVENT_PLANNING, "Event", "Event Planung", null),
  SOCIAL_MEDIA(DiscordGroup.SOCIAL_MEDIA, "Socials", "Social Media", null),
  CASTER(DiscordGroup.CASTER, "Caster", "Casting", null),
  DEVELOPER(DiscordGroup.DEVELOPER, "Dev", "Entwickler mit Kenntnissen in Java, HTML, SQL", null);


  private final DiscordGroup discordGroup;
  private final String name;
  private final String description;
  private final Emote emote;

  public SelectOption toSelectOption() {
    return name == null ? null : SelectOption.of(name, name()).withDescription(description).withEmoji(emote.getEmoji());
  }

  @Override
  public String toString() {
    return name;
  }

  public boolean isTeam() {
    return ordinal() <= TEAM_COACH.ordinal();
  }

  public boolean isOrga() {
    return ordinal() >= TEAM_COACH.ordinal();
  }

  @Nullable
  public static TeamPosition fromEmote(Emote emote) {
    return Arrays.stream(TeamPosition.values()).filter(position -> position.getEmote().equals(emote)).findFirst().orElse(null);
  }
}
