package de.xahrie.trues.api.discord.user;

import java.io.Serial;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.database.query.SQLEnum;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;
import de.xahrie.trues.api.datatypes.collections.SortedList;
import de.xahrie.trues.api.discord.group.DiscordGroup;
import de.xahrie.trues.api.discord.group.GroupAssignReason;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
@Table("discord_user_group")
public class DiscordUserGroup implements Entity<DiscordUserGroup> {
  @Serial
  private static final long serialVersionUID = 7171651659881865861L;

  @Setter
  private int id; // discord_user_group_id
  private final int userId; // discord_user
  private final DiscordGroup discordGroup; // discord_group
  private TimeRange range; // assign_time, permission_end
  private final GroupAssignReason reason; // reason
  private boolean active = false; // active

  public DiscordUserGroup merge(DiscordUserGroup other) {
    final SortedList<TimeRange> timeRanges = TimeRange.combine(SortedList.of(range, other.getRange()));
    final TimeRange newRange = timeRanges.size() < 2 ? timeRanges.getOr(timeRanges.size(), null) :
        timeRanges.stream().filter(timeRange -> timeRange.contains(LocalDateTime.now())).findFirst().orElse(timeRanges.get(0));
    if (!newRange.equals(range)) {
      this.range = newRange;
      new Query<>(DiscordUserGroup.class).col("assign_time", range.getStartTime()).col("permission_end", range.getEndTime()).update(id);
    }
    return this;
  }

  public DiscordUserGroup(@NotNull DiscordUser user, @NotNull DiscordGroup discordGroup, @NotNull TimeRange range) {
    this.user = user;
    this.userId = user.getId();
    this.discordGroup = discordGroup;
    this.range = range;
    this.reason = GroupAssignReason.ADD;
  }

  public DiscordUserGroup(int id, int userId, DiscordGroup discordGroup, TimeRange range, GroupAssignReason reason, boolean active) {
    this.id = id;
    this.userId = userId;
    this.discordGroup = discordGroup;
    this.range = range;
    this.reason = reason;
    this.active = active;
  }

  public static DiscordUserGroup get(List<Object> objects) {
    return new DiscordUserGroup(
        (int) objects.get(0),
        (int) objects.get(1),
        new SQLEnum<>(DiscordGroup.class).of(objects.get(2)),
        new TimeRange((LocalDateTime) objects.get(3), (LocalDateTime) objects.get(4)),
        new SQLEnum<>(GroupAssignReason.class).of(objects.get(5)),
        (boolean) objects.get(6)
    );
  }

  @Override
  public DiscordUserGroup create() {
    return new Query<>(DiscordUserGroup.class).key("discord_user", user).key("discord_group", discordGroup)
        .col("assign_time", range.getStartTime()).col("permission_end", range.getEndTime()).col("reason", reason).col("active", active)
        .insert(this);
  }

  public void end() {
    this.active = false;
    this.range = new TimeRange(range.getStartTime(), LocalDateTime.now());
    new Query<>(DiscordUserGroup.class).col("active", active).col("assign_time", range.getStartTime()).col("permission_end", range.getEndTime())
        .update(id);
  }

  public void setActive(boolean active) {
    this.active = active;
    new Query<>(DiscordUserGroup.class).col("active", active).update(id);
  }

  public void updateTimeRange(LocalDateTime start, int days) {
    final LocalDateTime end = range.getEndTime();
    final var timeRange = new TimeRange(start, Duration.ofDays(days));
    if (timeRange.getEndTime().isAfter(end)) {
      this.range = timeRange;
      new Query<>(DiscordUserGroup.class).col("assign_time", range.getStartTime()).col("permission_end", range.getEndTime()).update(id);
    }
  }

  private DiscordUser user;

  public DiscordUser getUser() {
    if (user != null) this.user = new Query<>(DiscordUser.class).entity(userId);
    return user;
  }
}
