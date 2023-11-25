package de.xahrie.trues.api.minecraft.beans;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import de.xahrie.trues.api.database.connector.SQLUtils;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.util.Util;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Table("mc_user")
@ExtensionMethod(SQLUtils.class)
public class MinecraftUser implements Entity<MinecraftUser> {
  @Serial
  private static final long serialVersionUID = 0L;

  @Setter
  private int id;
  private final UUID uuid;
  private String name;
  private final Integer discordUserId;
  private int timePlayed;
  private LocalDateTime lastOnline;
  private short deaths;
  private boolean whitelisted;
  private Integer teamId;
  private LocalDateTime joined;

  private DiscordUser discordUser;
  private MinecraftTeam minecraftTeam;

  public DiscordUser getDiscordUser() {
    if (discordUser == null)
      this.discordUser = new Query<>(DiscordUser.class).entity(discordUserId);
    return discordUser;
  }

  public MinecraftTeam getMinecraftTeam() {
    if (minecraftTeam == null)
      this.minecraftTeam = new Query<>(MinecraftTeam.class).entity(teamId);
    return minecraftTeam;
  }

  public MinecraftUser(@NotNull UUID uuid, @NotNull String name, @NotNull DiscordUser discordUser) {
    this.uuid = uuid;
    this.name = name;
    this.discordUser = discordUser;
    this.discordUserId = discordUser.getId();
    this.deaths = 0;
    this.timePlayed = 0;
    this.whitelisted = true;
    this.teamId = null;
    this.joined = null;
  }

  private MinecraftUser(int id, UUID uuid, String name, @Nullable Integer discordUserId, int timePlayed,
                        LocalDateTime lastOnline,
                        short deaths, boolean whitelisted, Integer teamId, LocalDateTime joined) {
    this.id = id;
    this.uuid = uuid;
    this.name = name;
    this.discordUserId = discordUserId;
    this.timePlayed = timePlayed;
    this.lastOnline = lastOnline;
    this.deaths = deaths;
    this.whitelisted = whitelisted;
    this.teamId = teamId;
    this.joined = joined;
  }

  public static MinecraftUser get(List<Object> objects) {
    return new MinecraftUser(
        (int) objects.get(0),
        UUID.fromString((String) objects.get(1)),
        (String) objects.get(2),
        SQLUtils.intValue(objects.get(3)),
        (int) objects.get(4),
        (LocalDateTime) objects.get(5),
        SQLUtils.shortValue(objects.get(6)),
        (boolean) objects.get(7),
        SQLUtils.intValue(objects.get(8)),
        (LocalDateTime) objects.get(9)
    );
  }

  @Override
  public MinecraftUser create() {
    return new Query<>(MinecraftUser.class).key("discord_user", discordUserId)
        .col("mc_uuid", uuid).col("mc_name", name).col("seconds_played", timePlayed)
        .col("last_time_online", lastOnline).col("deaths", deaths).col("whitelisted", whitelisted)
        .col("mc_team", teamId).col("team_joined", joined)
        .insert(this);
  }

  public void join(MinecraftTeam minecraftTeam) {
    if (minecraftTeam == null) {
      this.minecraftTeam = null;
      this.teamId = null;
      this.joined = null;
    } else if (this.minecraftTeam == null || (minecraftTeam.getId() != this.minecraftTeam.getId())) {
      this.minecraftTeam = minecraftTeam;
      this.teamId = minecraftTeam.getId();
      this.joined = LocalDateTime.now();
    }

    new Query<>(MinecraftUser.class).col("mc_team", teamId).col("team_joined", joined).update(id);
  }

  public void leave() {
    join(null);
  }

  public void setLastOnline(LocalDateTime lastOnline) {
    if (!Objects.equals(this.lastOnline, lastOnline))
      new Query<>(MinecraftUser.class).col("last_time_online", lastOnline).update(id);
    this.lastOnline = lastOnline;
  }

  public void setWhitelisted(boolean whitelisted) {
    if (this.whitelisted != whitelisted)
      new Query<>(MinecraftUser.class).col("whitelisted", whitelisted).update(id);
    this.whitelisted = whitelisted;
  }

  public void addDeath() {
    this.deaths++;
    new Query<>(MinecraftUser.class).col("deaths", deaths).update(id);
  }

  public void updateTimePlayed() {
    this.timePlayed = getPlayer().getStatistic(Statistic.PLAY_ONE_MINUTE) / (20 * 60);
    new Query<>(MinecraftUser.class).col("seconds_played", timePlayed).update(id);
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
    final ChatColor chatColor = Util.avoidNull(getMinecraftTeam(), ChatColor.GRAY, team -> team.getColor().getColor());
    final String nameToDisplay = chatColor + getPlayer().getName();
    final Player player = getPlayer().getPlayer();
    if (player != null) {
      player.setDisplayName(nameToDisplay);
      player.setPlayerListName(
              nameToDisplay + ChatColor.YELLOW + " " + player.getStatistic(Statistic.DEATHS));
    }
  }

  public void sendMessage(String message) {
    final OfflinePlayer player = getPlayer();
    if (player.isOnline())
      ((Player) player).sendMessage(message);
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
