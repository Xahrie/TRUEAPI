package de.xahrie.trues.api.calendar;

import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table("calendar")
public abstract class AbstractEventCalendar extends Calendar implements AThreadable {
  protected Long threadId; // thread_id

  public AbstractEventCalendar(TimeRange timeRange, String details, Long threadId) {
    super(timeRange, details);
    this.threadId = threadId;
  }

  protected AbstractEventCalendar(int id, TimeRange range, String details, Long threadId) {
    super(id, range, details);
    this.threadId = threadId;
  }
}
