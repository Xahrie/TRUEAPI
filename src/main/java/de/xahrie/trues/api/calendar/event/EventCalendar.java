package de.xahrie.trues.api.calendar.event;

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

/**
 * kurze Events
 */
@Getter
@Setter
@Table(value = "calendar", department = "event")
public class EventCalendar extends AbstractUserCalendar implements Entity<EventCalendar> {
  @Serial
  private static final long serialVersionUID = -2357919003996341997L;
  private Long threadId;

  public void setThreadId(long threadId) {
    if (this.threadId.equals(threadId)) return;
    new Query<>(EventCalendar.class).col("thread_id", threadId).update(id);
    this.threadId = threadId;
  }

  public EventCalendar(TimeRange timeRange, String details, DiscordUser creator) {
    super(timeRange, details, creator);
  }

  private EventCalendar(int id, TimeRange range, String details, int creatorId, long threadId) {
    super(id, range, details, creatorId);
    this.threadId = threadId;
  }

  public static EventCalendar get(List<Object> objects) {
    return new EventCalendar(
        (int) objects.get(0),
        new TimeRange((LocalDateTime) objects.get(2), (LocalDateTime) objects.get(3)),
        (String) objects.get(4),
        (int) objects.get(7),
        (Long) objects.get(6)
    );
  }

  @Override
  public EventCalendar create() {
    return new Query<>(EventCalendar.class)
        .col("calendar_start", range.getStartTime()).col("calendar_end", range.getEndTime()).col("details", details)
        .col("discord_user", user.getId()).col("thread_id", threadId)
        .insert(this);
  }

  private Event event;

  public Event getEvent() {
    if (event == null) this.event = new Query<>(Event.class).where("calendar", id).entity();
    return event;
  }
}
