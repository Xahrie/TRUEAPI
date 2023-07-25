package de.xahrie.trues.api.calendar.scheduling;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;
import de.xahrie.trues.api.calendar.AbstractUserCalendar;
import de.xahrie.trues.api.discord.user.DiscordUser;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(value = "calendar", department = "schedule")
public class SchedulingCalendar extends AbstractUserCalendar implements Entity<SchedulingCalendar> {
  @Serial
  private static final long serialVersionUID = -3658276021282430693L;

  public SchedulingCalendar(TimeRange timeRange, String details, DiscordUser discordUser) {
    super(timeRange, details, discordUser);
  }

  private SchedulingCalendar(int id, TimeRange range, String details, int userId) {
    super(id, range, details, userId);
  }

  public static SchedulingCalendar get(List<Object> objects) {
    return new SchedulingCalendar(
        (int) objects.get(0),
        new TimeRange((LocalDateTime) objects.get(2), (LocalDateTime) objects.get(3)),
        (String) objects.get(4),
        (int) objects.get(7)
    );
  }

  @Override
  public SchedulingCalendar create() {
    return new Query<>(SchedulingCalendar.class)
        .col("calendar_start", range.getStartTime()).col("calendar_end", range.getEndTime()).col("details", details)
        .col("discord_user", user)
        .insert(this);
  }
}
