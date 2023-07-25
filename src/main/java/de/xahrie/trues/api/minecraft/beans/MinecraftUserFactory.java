package de.xahrie.trues.api.minecraft.beans;

import de.xahrie.trues.api.database.query.Query;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class MinecraftUserFactory {
  @Nullable
  public static MinecraftUser findUser(@NonNull Player player) {
    return new Query<>(MinecraftUser.class).where("mc_uuid", player.getUniqueId().toString()).entity();
  }
}
