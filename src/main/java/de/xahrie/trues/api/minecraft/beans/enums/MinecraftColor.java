package de.xahrie.trues.api.minecraft.beans.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.xahrie.trues.api.database.connector.Listing;
import de.xahrie.trues.api.database.connector.SQLUtils;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.minecraft.beans.MinecraftTeam;
import de.xahrie.trues.api.util.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;

/**
 * Created by Lara on 24.11.2022 for MCProject
 */
@Getter
@RequiredArgsConstructor
@Listing(Listing.ListingType.ORDINAL)
public enum MinecraftColor {
  BLACK("0"),
  DARK_BLUE("1"),
  DARK_GREEN("2"),
  DARK_AQUA("3"),
  DARK_RED("4"),
  DARK_PURPLE("5"),
  GOLD("6"),
  GRAY("7"),
  DARK_GRAY("8"),
  BLUE("9"),
  GREEN("a"),
  AQUA("b"),
  RED("c"),
  LIGHT_PURPLE("d"),
  YELLOW("e"),
  WHITE("f"),
  MAGIC("k");

  private final String code;

  public static MinecraftColor fromChar(char code) {
    return fromChar(code + "");
  }

  public static MinecraftColor fromChar(String code) {
    for (MinecraftColor minecraftColor : values()) {
      if (minecraftColor.getCode().equals(code)) {
        return minecraftColor;
      }
    }
    return null;
  }

  public static MinecraftColor fromString(String colorName) {
    return MinecraftColor.valueOf(StringUtils.upper(colorName));
  }

  @Override
  public String toString() {
    return StringUtils.capitalizeEnum(name());
  }

  public ChatColor getColor() {
    return ChatColor.valueOf(name());
  }

  public static MinecraftColor random() {
    List<Object[]> used = new Query<>(MinecraftTeam.class).get("color", MinecraftColor.class).list();
    final List<Integer> usedIds = used.stream().map(objects -> SQLUtils.intValue(objects[0])).toList();
    List<MinecraftColor> minecraftColors = new java.util.ArrayList<>(
        Arrays.stream(values()).filter(minecraftColor -> !usedIds.contains(minecraftColor.ordinal())).toList()
    );
    if (minecraftColors.isEmpty())
      minecraftColors = new java.util.ArrayList<>(Arrays.stream(values()).toList());

    Collections.shuffle(minecraftColors);
    return minecraftColors.get(0);
  }
}
