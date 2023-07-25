package de.xahrie.trues.api.calendar;

import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Id;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
@Setter
@Table("calendar")
public abstract class Calendar implements ACalendar, Id, Comparable<Calendar> {
  protected int id; // calendar_id
  protected TimeRange range; // calendar_start, calendar_end
  protected String details; // details

  public Calendar(TimeRange timeRange, String details) {
    this.range = timeRange;
    this.details = details;
  }

  public void setRange(TimeRange range) {
    this.range = range;
    new Query<>(Calendar.class).col("calendar_start", range.getStartTime()).col("calendar_end", range.getEndTime()).update(id);
  }

  @Override
  public int compareTo(@NotNull Calendar o) {
    return range.getStartTime().compareTo(o.getRange().getStartTime());
  }
}
