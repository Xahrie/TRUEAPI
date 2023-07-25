package de.xahrie.trues.api.calendar;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;
import de.xahrie.trues.api.discord.user.DiscordUser;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(value = "calendar", department = "app")
public class ApplicationCalendar extends AbstractUserCalendar implements
        Entity<ApplicationCalendar>, AThreadable {
  @Serial
  private static final long serialVersionUID = -3831437593024647108L;

  private Long threadId;

  public ApplicationCalendar(TimeRange timeRange, String details, DiscordUser discordUser, Long threadId) {
    super(timeRange, details, discordUser);
    this.threadId = threadId;
  }

  public ApplicationCalendar(int id, TimeRange range, String details, int userId, Long threadId) {
    super(id, range, details, userId);
    this.threadId = threadId;
  }

  public static ApplicationCalendar get(List<Object> objects) {
    return new ApplicationCalendar(
        (int) objects.get(0),
        new TimeRange((LocalDateTime) objects.get(2), (LocalDateTime) objects.get(3)),
        (String) objects.get(4),
        (int) objects.get(7),
        (Long) objects.get(6)
    );
  }

  @Override
  public ApplicationCalendar create() {
    return new Query<>(ApplicationCalendar.class)
        .col("calendar_start", range.getStartTime())
        .col("calendar_end", range.getEndTime())
        .col("details", details)
        .col("discord_user", userId)
        .col("thread_id", threadId).insert(this);
  }
}
