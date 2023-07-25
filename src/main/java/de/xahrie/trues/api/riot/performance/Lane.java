package de.xahrie.trues.api.riot.performance;

import java.util.List;

import de.xahrie.trues.api.database.connector.Listing;
import de.xahrie.trues.api.discord.group.DiscordGroup;
import de.xahrie.trues.api.discord.group.Roleable;
import de.xahrie.trues.api.discord.message.Emote;
import de.xahrie.trues.api.discord.util.Jinx;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import no.stelar7.api.r4j.basic.constants.types.lol.LaneType;

@RequiredArgsConstructor
@Getter
@Listing(Listing.ListingType.ORDINAL)
public enum Lane implements Roleable {
  UNKNOWN("null", null),
  TOP("Top", Emote.TOP.getEmoji()),
  JUNGLE("Jungle", Emote.JUNGLE.getEmoji()),
  MIDDLE("Middle", Emote.MIDDLE.getEmoji()),
  BOTTOM("Bottom", Emote.BOTTOM.getEmoji()),
  UTILITY("Support", Emote.SUPPORT.getEmoji());

  public SelectOption toSelectOption() {
    return SelectOption.of(displayName, name()).withEmoji(emoji);
  }

  public static ActionRow ACTION_ROW() {
    final List<SelectOption> selectOptions = ITERATE.stream().map(Lane::toSelectOption).toList();
    return ActionRow.of(StringSelectMenu.create("lol-position").setPlaceholder("Lane").addOptions(selectOptions).build());
  }

  public static final List<Lane> ITERATE = List.of(TOP, JUNGLE, MIDDLE, BOTTOM, UTILITY);
  private final String displayName;
  private final Emoji emoji;

  @Override
  public String toString() {
    return displayName;
  }

  public Role getRole() {
    return Jinx.instance.getGuild().getRoleById(getDiscordId());
  }

  public Role getHelperRole() {
    return Jinx.instance.getGuild().getRoleById(getDiscordIdHelp());
  }

  public long getDiscordId() {
    return DiscordGroup.valueOf(name()).getDiscordId();
  }

  public Long getDiscordIdHelp() {
    return DiscordGroup.valueOf(name() + "_HELP").getDiscordId();
  }

  public static Lane transform(LaneType lane) {
    return switch (lane) {
      case NONE, INVALID, AFK -> UNKNOWN;
      case BOT -> BOTTOM;
      case MID -> MIDDLE;
      default -> Lane.valueOf(lane.name());
    };
  }
}
