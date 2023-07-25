package de.xahrie.trues.api.scheduler;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;

import de.xahrie.trues.api.util.StringUtils;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(StringUtils.class)
public class ScheduleComparer {
  private final Schedule schedule;
  private final LocalDateTime now = LocalDateTime.now();

  public ScheduleComparer(Schedule schedule) {
    this.schedule = schedule;
  }

  public boolean test() {
    return check(ChronoField.MINUTE_OF_HOUR, schedule.minute())
        && check(ChronoField.HOUR_OF_DAY, schedule.hour())
        && check(ChronoField.DAY_OF_WEEK, schedule.dayOfWeek())
        && check(ChronoField.DAY_OF_MONTH, schedule.dayOfMonth())
        && check(ChronoField.MONTH_OF_YEAR, schedule.month())
        && check(ChronoField.YEAR, schedule.year());
  }

  private boolean check(TemporalField field, String value) {
    final int currentValue = now.get(field);
    final Integer every = value.after("%").intValue(null);
    return value.equals("*") || (value.contains("%") && every != null && currentValue % every == 0) || value.equals(String.valueOf(currentValue));
  }
}
