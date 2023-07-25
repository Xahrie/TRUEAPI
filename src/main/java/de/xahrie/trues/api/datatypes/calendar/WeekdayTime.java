package de.xahrie.trues.api.datatypes.calendar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

public record WeekdayTime(DayOfWeek day, LocalTime time) {
  public static WeekdayTime min(DayOfWeek day) {
    return new WeekdayTime(day, LocalTime.MIN);
  }

  public static WeekdayTime max(DayOfWeek day) {
    return new WeekdayTime(day, LocalTime.MAX);
  }

  public LocalDateTime nextOrCurrent(LocalDateTime dateTime) {
    return nextOrCurrent(dateTime.toLocalDate());
  }

  public LocalDateTime nextOrCurrent(LocalDate date) {
    date = date.with(TemporalAdjusters.nextOrSame(day));
    return LocalDateTime.of(date, time);
  }
}
