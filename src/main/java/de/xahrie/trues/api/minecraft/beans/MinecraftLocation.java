package de.xahrie.trues.api.minecraft.beans;

import java.util.Objects;

import de.xahrie.trues.api.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record MinecraftLocation(String worldName, Integer x, Integer y, Integer height) {

  @NotNull
  @Contract("null -> new")
  public static MinecraftLocation of(@Nullable Location location) {
    if (location == null) return new MinecraftLocation(null, null, null, null);
    if (location.getWorld() == null) throw new IllegalArgumentException("World is not loaded");
    return new MinecraftLocation(location.getWorld().getName(), location.getBlockX(), location.getBlockZ(),
        location.getBlockY());
  }

  public World getWorld() {
    return Util.avoidNull(worldName, Bukkit.getWorld("world"), Bukkit::getWorld);
  }

  @Nullable
  public Location getLocation() {
    return Util.avoidNull(x, x -> new Location(getWorld(), x, height, y));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final MinecraftLocation that = (MinecraftLocation) o;
    return Objects.equals(worldName, that.worldName) &&
        Objects.equals(x, that.x) && Objects.equals(y, that.y) &&
        Objects.equals(height, that.height);
  }

  @Override
  public int hashCode() {
    return Objects.hash(worldName, x, y, height);
  }
}
