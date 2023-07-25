package de.xahrie.trues.api.community.application;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.database.query.SQLEnum;
import de.xahrie.trues.api.discord.user.DiscordUser;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table("application")
public class Application implements Entity<Application> {
  @Serial
  private static final long serialVersionUID = 8214282036590463912L;

  private int id;
  private final int userId;
  private TeamRole role;
  private final TeamPosition position;
  private LocalDateTime appTimestamp = LocalDateTime.now();
  private boolean active;
  private String appNotes;

  private DiscordUser user;

  public DiscordUser getUser() {
    if (user == null) this.user = new Query<>(DiscordUser.class).entity(userId);
    return user;
  }

  public Application(DiscordUser user, TeamRole role, TeamPosition position, boolean active, String appNotes) {
    this.user = user;
    this.userId = user.getId();
    this.role = role;
    this.position = position;
    this.active = active;
    this.appNotes = appNotes;
  }

  private Application(int id, int userId, TeamRole role, TeamPosition position, LocalDateTime appTimestamp, boolean active, String appNotes) {
    this.id = id;
    this.userId = userId;
    this.role = role;
    this.position = position;
    this.appTimestamp = appTimestamp;
    this.active = active;
    this.appNotes = appNotes;
  }

  @Override
  public String toString() {
    return role.name() + " - " + position.name() + "\n" + appNotes;
  }

  public static Application get(List<Object> objects) {
    return new Application(
        (int) objects.get(0),
        (int) objects.get(1),
        new SQLEnum<>(TeamRole.class).of(objects.get(2)),
        new SQLEnum<>(TeamPosition.class).of(objects.get(3)),
        (LocalDateTime) objects.get(4),
        (boolean) objects.get(5),
        (String) objects.get(6)
    );
  }

  @Override
  public Application create() {
    return new Query<>(Application.class)
        .key("discord_user", userId).key("position", position)
        .col("lineup_role", role).col("app_timestamp", appTimestamp).col("waiting", active).col("app_notes", appNotes)
        .insert(this);
  }
}
