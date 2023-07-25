package de.xahrie.trues.api.minecraft;

import de.xahrie.trues.api.minecraft.beans.MinecraftUser;
import lombok.Data;
import org.bukkit.entity.Player;

import java.util.List;

@Data
public abstract class SubCommand {
  private final String name;

  public abstract String getDescription();

  public abstract String getSyntax();

  public abstract void perform(Player player, String[] args, MinecraftUser user);

  public abstract List<String> getArguments(Player player, String[] args);

  public abstract boolean isValidForUser(MinecraftUser user);

}
