package de.xahrie.trues.api.calendar;

import java.io.Serial;
import java.util.List;

import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(value = "calendar", department = "match")
public class MatchCalendar extends Calendar implements Entity<MatchCalendar> {
  @Serial
  private static final long serialVersionUID = -2357919003996341997L;

  public MatchCalendar(TimeRange timeRange, String details) {
    super(timeRange, details);
  }

  public static MatchCalendar get(List<Object> objects) {
    throw new IllegalArgumentException("Cannot be created");
  }

  @Override
  public MatchCalendar create() {
    throw new IllegalArgumentException("Cannot be created");
  }
}
