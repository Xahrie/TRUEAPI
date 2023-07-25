package de.xahrie.trues.api.calendar.event;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;
import de.xahrie.trues.api.calendar.Calendar;
import lombok.Getter;
import lombok.Setter;

/**
 * lange Events
 */
@Getter
@Setter
@Table(value = "calendar", department = "seasonevent")

public class SeasonEventCalendar extends Calendar implements Entity<SeasonEventCalendar> {
  @Serial
  private static final long serialVersionUID = -2357919003996341997L;

  public SeasonEventCalendar(TimeRange timeRange, String details) {
    super(timeRange, details);
  }

  private SeasonEventCalendar(int id, TimeRange range, String details) {
    super(id, range, details);
  }

  public static SeasonEventCalendar get(List<Object> objects) {
    return new SeasonEventCalendar(
        (int) objects.get(0),
        new TimeRange((LocalDateTime) objects.get(2), (LocalDateTime) objects.get(3)),
        (String) objects.get(4)
    );
  }

  @Override
  public SeasonEventCalendar create() {
    return new Query<>(SeasonEventCalendar.class)
        .col("calendar_start", range.getStartTime()).col("calendar_end", range.getEndTime()).col("details", details)
        .insert(this);
  }
}
