package de.xahrie.trues.api.minecraft.beans;

import java.util.List;
import java.util.Objects;

import de.xahrie.trues.api.database.connector.SQLUtils;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.database.query.SQLEnum;
import de.xahrie.trues.api.minecraft.beans.enums.MinecraftColor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lara on 23.11.2022 for MCProject
 */
@Table("mc_team")
@Getter
@Setter
public final class MinecraftTeam implements Entity<MinecraftTeam> {
  private int id;
  private String name;
  private String abbreviation;
  private String password;
  private MinecraftColor color;
  private MinecraftLocation center;
  private int creatorId;

  private MinecraftUser creator;

  public MinecraftUser getCreator() {
    if (creator == null)
      this.creator = new Query<>(MinecraftUser.class).entity(creatorId);
    return creator;
  }

  public MinecraftTeam(@NotNull String name, @NotNull String abbreviation, @NotNull String password,
                       @NotNull MinecraftUser creator) {
    this.name = name;
    this.abbreviation = abbreviation;
    this.password = password;
    this.color = MinecraftColor.random();
    this.creator = creator;
    this.creatorId = creator.getId();
  }

  public MinecraftTeam(@NotNull String name, @NotNull String abbreviation, @NotNull String password,
                       @NotNull MinecraftColor color, @NotNull MinecraftUser creator) {
    this.name = name;
    this.abbreviation = abbreviation;
    this.password = password;
    this.color = color;
    this.creator = creator;
    this.creatorId = creator.getId();
  }

  private MinecraftTeam(int id, @NotNull String name, @NotNull String abbreviation, @NotNull String password,
                        @NotNull MinecraftColor color, MinecraftLocation center, int creatorId) {
    this.id = id;
    this.name = name;
    this.abbreviation = abbreviation;
    this.password = password;
    this.color = color;
    this.center = center;
    this.creatorId = creatorId;
  }

  @Override
  public MinecraftTeam create() {
    return new Query<>(MinecraftTeam.class).key("team_name", name)
        .col("team_abbr", abbreviation).col("team_password", password).col("color", color)
        .col("center_x", center.x()).col("center_y", center.y()).col("center_height", center.height())
        .col("center_world", center.worldName()).col("creator", creatorId)
        .insert(this);
  }

  @NotNull
  @Contract("_ -> new")
  public static MinecraftTeam get(@NotNull List<Object> objects) {
    return new MinecraftTeam(
        (int) objects.get(0),
        (String) objects.get(1),
        (String) objects.get(2),
        (String) objects.get(3),
        new SQLEnum<>(MinecraftColor.class).of(objects.get(4)),
        new MinecraftLocation(
            (String) objects.get(9),
            SQLUtils.intValue(objects.get(5)),
            SQLUtils.intValue(objects.get(6)),
            SQLUtils.intValue(objects.get(8))
        ),
        (int) objects.get(7)
    );
  }

  public void setName(String name) {
    if (!this.name.equals(name))
      new Query<>(MinecraftTeam.class).col("team_name", name).update(id);
    this.name = name;
  }

  public void setColor(MinecraftColor color) {
    if (this.color == color)
      new Query<>(MinecraftTeam.class).col("color", color).update(id);
    this.color = color;
  }

  public void setAbbreviation(String abbreviation) {
    if (!this.abbreviation.equals(abbreviation))
      new Query<>(MinecraftTeam.class).col("team_abbr", abbreviation).update(id);
    this.abbreviation = abbreviation;
  }

  public void setPassword(String password) {
    if (!this.password.equals(password))
      new Query<>(MinecraftTeam.class).col("team_password", password).update(id);
    this.password = password;
  }

  public void setCenter(Location location) {
    final MinecraftLocation newCenter = MinecraftLocation.of(location);
    if (Objects.equals(this.center, newCenter)) return;

    new Query<>(MinecraftTeam.class).col("center_x", newCenter.x()).col("center_y", newCenter.y())
        .col("center_height", newCenter.height()).col("center_world", newCenter.worldName()).update(id);
    this.center = newCenter;
  }

  public void setCreator(@NotNull MinecraftUser creator) {
    if (Objects.equals(this.creator, creator))
      new Query<>(MinecraftTeam.class).col("creator", creator.getId()).update(id);
    this.creator = creator;
    this.creatorId = creator.getId();
  }

  @NotNull
  public List<MinecraftUser> getMembers() {
    return new Query<>(MinecraftUser.class).where("mc_team", getId()).entityList();
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof MinecraftTeam)) return false;

    return this.id == ((MinecraftTeam) other).getId() || this.name.equals(((MinecraftTeam) other).getName()) ||
        this.abbreviation.equals(((MinecraftTeam) other).getAbbreviation());
  }

  public String getDisplay() {
    return name + " (" + abbreviation + ")";
  }

  public void sendTeamMessage(@NotNull Player source, @NotNull String message) {
    for (MinecraftUser survivalUser : getMembers()) {
      final Player survivalPlayer = survivalUser.getPlayer().getPlayer();
      if (survivalPlayer == null) continue;

      survivalPlayer.sendMessage(ChatColor.YELLOW + "Team | " + ChatColor.BOLD + source.getDisplayName() + ChatColor.RESET +
          ChatColor.DARK_GRAY + " | " + ChatColor.GRAY + message);
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, abbreviation, color, creator);
  }

  @Override
  public String toString() {
    return "MinecraftTeam{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", abbreviation='" + abbreviation + '\'' +
        ", password='" + password + '\'' +
        ", color=" + color +
        ", center=" + center +
        ", creatorId=" + creatorId +
        ", creator=" + creator +
        '}';
  }
}
