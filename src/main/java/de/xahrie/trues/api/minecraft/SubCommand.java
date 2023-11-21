package de.xahrie.trues.api.minecraft;

import de.xahrie.trues.api.minecraft.beans.MinecraftUser;
import lombok.Data;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

@Data
public abstract class SubCommand {
  private final String name;

  public abstract String getDescription();

  public abstract String getSyntax();

  public abstract boolean perform(Player player, String[] args, MinecraftUser user);

  public abstract List<String> getArguments(Player player, String[] args);

  public abstract boolean isValidForUser(MinecraftUser user);

  public boolean error(Player player, String message) {
    player.sendMessage(ChatColor.RED + message);
    return false;
  }

  public boolean message(Player player, String message) {
    player.sendMessage(message);
    return false;
  }
}
