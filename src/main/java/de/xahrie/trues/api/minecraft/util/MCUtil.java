package de.xahrie.trues.api.minecraft.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class MCUtil {
  public static String camel(String text, boolean replace) {
    if (replace) {
      text = text.replace("_", " ");
    }
    StringBuilder builder = new StringBuilder();
    boolean nextCharToLower = false;
    for (int i = 0; i < text.length(); i++) {
      char currentChar = text.charAt(i);
      if (currentChar == ' ') {
        builder.append(" ");
        nextCharToLower = false;
        continue;
      }
      builder.append(nextCharToLower ? Character.toLowerCase(currentChar) : Character.toUpperCase(currentChar));
      nextCharToLower = true;
    }
    return builder.toString();
  }

  public static String upper(String text, boolean replace) {
    if (replace) {
      text = text.replace(" ", "_");
    }
    return text.toUpperCase();
  }

  public static void handleNightSkipping() {
    final List<? extends Player> sleeping = Bukkit.getOnlinePlayers().stream().filter(Player::isSleeping).toList();
    if (sleeping.size() >= Bukkit.getOnlinePlayers().size() * .34) {
      Bukkit.broadcastMessage(ChatColor.WHITE + "SLEEPY TIME!!!");
    }
  }
}
