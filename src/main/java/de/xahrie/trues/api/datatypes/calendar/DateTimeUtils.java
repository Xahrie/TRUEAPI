package de.xahrie.trues.api.datatypes.calendar;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import org.joda.time.DateTime;

public class DateTimeUtils {
  public static long getMinutes(Duration duration) {
    return Math.round(duration.getSeconds() / 60.);
  }
  public static int daysBetween(DayOfWeek startDay, DayOfWeek endDay) {
    final int daysBetween = endDay.getValue() - startDay.getValue();
    return daysBetween < 0 ? daysBetween + 7 : daysBetween;
  }

  public static boolean isBetween(LocalDateTime localDateTime, LocalDate start, LocalDate end) {
    final LocalDate date = localDateTime.toLocalDate();
    if (date.isEqual(start) || date.isEqual(end)) return true;
    return date.isAfter(start) && date.isBefore(end);
  }

  public static boolean isBetween(LocalDateTime dateTime, LocalDateTime start, LocalDateTime end) {
    return isAfterEqual(dateTime, start) && isBeforeEqual(dateTime, end);
  }

  public static boolean isAfterEqual(LocalDateTime localDateTime, LocalDateTime other) {
    return localDateTime.isAfter(other) || localDateTime.isEqual(other);
  }

  public static boolean isBeforeEqual(LocalDateTime localDateTime, LocalDateTime other) {
    return localDateTime.isBefore(other) || localDateTime.isEqual(other);
  }

  public static boolean isAfterEqual(LocalTime localDateTime, LocalTime other) {
    return localDateTime.isAfter(other) || localDateTime.equals(other);
  }

  public static boolean isBeforeEqual(LocalTime localDateTime, LocalTime other) {
    return localDateTime.isBefore(other) || localDateTime.equals(other);
  }

  public static LocalDateTime min(LocalDateTime localDateTime, LocalDateTime other) {
    return localDateTime.isBefore(other) ? localDateTime : other;
  }

  public static LocalDateTime max(LocalDateTime localDateTime, LocalDateTime other) {
    return localDateTime.isAfter(other) ? localDateTime : other;
  }

  public static LocalDateTime fromEpoch(int epochSeconds) {
    final Instant instant = Instant.ofEpochSecond(epochSeconds);
    return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
  }

  public static DateTime toJoda(LocalDateTime localDateTime) {
    return new DateTime(localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
  }
}
