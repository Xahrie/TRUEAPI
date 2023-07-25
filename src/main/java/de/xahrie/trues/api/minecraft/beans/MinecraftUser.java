package de.xahrie.trues.api.minecraft.beans;

import de.xahrie.trues.api.database.connector.SQLUtils;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.discord.user.DiscordUser;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import java.io.Serial;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Table("minecraft_user")
@ExtensionMethod(SQLUtils.class)
public class MinecraftUser implements Entity<MinecraftUser> {
  @Serial
  private static final long serialVersionUID = 0L;

  @Setter
  private int id;
  private final UUID uuid;
  private String name;
  private final int discordUserId;
  private int timePlayed;
  private short deaths;

  private DiscordUser discordUser;

  public DiscordUser getDiscordUser() {
    if (discordUser == null) {
      this.discordUser = new Query<>(DiscordUser.class).entity(discordUserId);
    }
    return discordUser;
  }

  public MinecraftUser(UUID uuid, String name, DiscordUser discordUser) {
    this.uuid = uuid;
    this.name = name;
    this.discordUser = discordUser;
    this.discordUserId = discordUser.getId();
    this.deaths = 0;
    this.timePlayed = 0;
  }

  private MinecraftUser(int id, UUID uuid, String name, int discordUserId, int timePlayed, short deaths) {
    this.id = id;
    this.uuid = uuid;
    this.name = name;
    this.discordUserId = discordUserId;
    this.timePlayed = timePlayed;
    this.deaths = deaths;
  }

  public static MinecraftUser get(List<Object> objects) {
    return new MinecraftUser(
            (int) objects.get(0),
            UUID.fromString((String) objects.get(1)),
            (String) objects.get(2),
            (int) objects.get(3),
            (int) objects.get(4),
            objects.get(5).shortValue()
    );
  }

  @Override
  public MinecraftUser create() {
    return new Query<>(MinecraftUser.class).key("discord_user", discordUserId)
            .col("mc_uuid", uuid).col("mc_name", name).col("time_played", timePlayed)
            .col("deaths", deaths).insert(this);
  }

  public void addDeath() {
    this.deaths++;
    new Query<>(MinecraftUser.class).col("deaths", deaths).update(id);
  }

  public void updateTimePlayed() {
    this.timePlayed = getPlayer().getStatistic(Statistic.PLAY_ONE_MINUTE) / (20 * 60);
    new Query<>(MinecraftUser.class).col("time_played", timePlayed).update(id);
  }

  public void updateName() {
    final String newName = getPlayer().getName();
    if (!name.equals(newName)) new Query<>(MinecraftUser.class).col("mc_name", newName).update(id);
    this.name = newName;
  }

  public String getTimePlayedString() {
    updateTimePlayed();
    if (timePlayed < 60) return ":" + timePlayed;
    else if (timePlayed < 600) return (timePlayed / 60) + ":" + Math.round((timePlayed % 60) / 6.);
    return Math.round(timePlayed / 60.) + "h";
  }

  public void handleName() {
    updateName();
    final String nameToDisplay = ChatColor.GRAY + getPlayer().getName();
    final Player player = getPlayer().getPlayer();
    if (player != null) {
      player.setDisplayName(nameToDisplay);
      player.setPlayerListName(
              nameToDisplay + ChatColor.YELLOW + " " + player.getStatistic(Statistic.DEATHS));
    }
  }

  public OfflinePlayer getPlayer() {
    final OfflinePlayer player = Bukkit.getPlayer(this.uuid);
    return player == null ? Bukkit.getOfflinePlayer(this.uuid) : player;
  }

  public String getName() {
    return getPlayer().getName() == null ? name : getPlayer().getName();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MinecraftUser that = (MinecraftUser) o;
    return uuid.equals(that.uuid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid);
  }
}
