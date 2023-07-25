package de.xahrie.trues.api.datatypes.calendar;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import de.xahrie.trues.api.datatypes.collections.SortedList;
import de.xahrie.trues.api.calendar.TeamCalendar;
import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;

@Getter
@ExtensionMethod(DateTimeUtils.class)
public class TimeRange implements Comparable<TimeRange> {
  private final LocalDateTime startTime;
  private final LocalDateTime endTime;

  public TimeRange(LocalDateTime startTime, Duration duration) {
    this(startTime, startTime.plus(duration));
  }

  public TimeRange(LocalDateTime startTime, LocalDateTime endTime) {
    this.startTime = startTime;
    if (endTime != null && endTime.isBefore(startTime)) endTime = endTime.plusDays(1);
    this.endTime = endTime;
  }

  public String display() {
    return TimeFormat.AUTO.of(startTime);
  }

  public String displayRange() {
    if (startTime.isBefore(LocalDateTime.now())) return "Ende " + TimeFormat.AUTO.of(endTime);
    return "Beginn " + TimeFormat.AUTO.of(startTime);
  }

  public String duration() {
    final long minutes = Duration.between(startTime, endTime).get(ChronoUnit.MINUTES);
    final int realMinutes = (int) (minutes % 60);
    return minutes / 60 + ":" + (realMinutes < 10 ? "0" : "") + realMinutes + " Stunden";
  }

  public boolean contains(LocalDateTime time) {
    return time.isBetween(startTime, endTime);
  }

  public String trainingReserved(OrgaTeam orgaTeam) {
    return isReserved(orgaTeam) ? "ja" : "nein";
  }

  private boolean isReserved(OrgaTeam orgaTeam) {
    for (TeamCalendar calendarEntry : orgaTeam.getScheduler().getCalendarEntries()) {
      final TimeRange range = calendarEntry.getRange();
      if (range.getStartTime().isAfterEqual(endTime)) continue;
      if (range.getEndTime().isBeforeEqual(startTime)) continue;
      return true;
    }
    return false;
  }

  public TimeRange plusWeeks(int weeks) {
    return new TimeRange(startTime.plusWeeks(weeks), endTime.plusWeeks(weeks));
  }

  public boolean hasStarted() {
    return startTime.isAfter(LocalDateTime.now());
  }

  public boolean hasRunning() {
    return hasStarted() && !hasEnded();
  }

  public boolean hasEnded() {
    return endTime.isAfter(LocalDateTime.now());
  }

  public static List<TimeRange> reduce(List<TimeRange> from, List<TimeRange> minus) {
    from.sort(TimeRange::compareTo);
    minus.sort(TimeRange::compareTo);
    final ArrayList<TimeRange> newTimeRanges = new ArrayList<>(from);
    for (TimeRange minusRange : minus) {
      for (final TimeRange newRange : newTimeRanges) {
        if (minusRange.getStartTime().isBeforeEqual(newRange.getStartTime())) {
          if (minusRange.getEndTime().isBeforeEqual(newRange.getStartTime())) continue;
          newTimeRanges.remove(newRange);
          if (minusRange.getEndTime().isBefore(newRange.getEndTime())) {
            newTimeRanges.add(new TimeRange(minusRange.getEndTime(), newRange.getEndTime()));
          }
          continue;
        }

        if (minusRange.getEndTime().isAfterEqual(newRange.getEndTime())) {
          if (minusRange.getStartTime().isAfterEqual(newRange.getEndTime())) continue;
          newTimeRanges.remove(newRange);
          if (minusRange.getStartTime().isAfter(newRange.getStartTime())) {
            newTimeRanges.add(new TimeRange(newRange.getStartTime(), minusRange.getStartTime()));
          }
          continue;
        }

        newTimeRanges.remove(newRange);
        newTimeRanges.add(new TimeRange(newRange.getStartTime(), minusRange.getStartTime()));
        newTimeRanges.add(new TimeRange(minusRange.getEndTime(), newRange.getEndTime()));
      }
    }
    return newTimeRanges;
  }

  public static SortedList<TimeRange> combine(SortedList<TimeRange> timeRanges) {
    final SortedList<TimeRange> rangesNew = SortedList.sorted();
    timeRanges.sort();
    if (timeRanges.size() < 2) return timeRanges;
    TimeRange rangeOld = timeRanges.get(0);
    for (TimeRange range : new ArrayList<>(timeRanges).subList(1, timeRanges.size())) {
      if (rangeOld.getEndTime().isAfterEqual(range.getStartTime())) {
        rangeOld = new TimeRange(rangeOld.getStartTime().min(range.getStartTime()), rangeOld.getEndTime().max(range.getEndTime()));
      } else {
        rangesNew.add(rangeOld);
        rangeOld = range;
      }
    }
    rangesNew.add(rangeOld);
    return rangesNew;
  }

  public static List<TimeRange> intersect(List<TimeRange> ranges1, List<TimeRange> ranges2) {
    return combine(SortedList.sorted(ranges1.stream().flatMap(range1 -> range1.intersect(ranges2).stream())));
  }

  private List<TimeRange> intersect(List<TimeRange> ranges) {
    final List<TimeRange> newRanges = new ArrayList<>();
    for (final TimeRange range : ranges) {
      if (range.getStartTime().isBeforeEqual(endTime) && startTime.isBeforeEqual(range.getEndTime())) {
        final LocalDateTime start = range.getStartTime().isAfter(startTime) ? range.getStartTime() : startTime;
        final LocalDateTime end = range.getEndTime().isBefore(endTime) ? range.getEndTime() : endTime;
        newRanges.add(new TimeRange(start, end));
      }
    }
    return newRanges;
  }

  @Override
  public int compareTo(@NotNull TimeRange o) {
    return Comparator.comparing(TimeRange::getStartTime).thenComparing(TimeRange::getEndTime).compare(this, o);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof final TimeRange timeRange)) return false;
    return startTime.equals(timeRange.getStartTime()) && endTime.equals(timeRange.getEndTime());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getStartTime(), getEndTime());
  }

  @Override
  public String toString() {
    LocalDateTime s = startTime;
    final List<String> outputs = new ArrayList<>();
    while (s.isBefore(endTime)) {
      final LocalTime start = (s.toLocalDate().equals(startTime.toLocalDate())) ? startTime.toLocalTime() : LocalTime.MIN;
      final LocalTime end = (s.toLocalDate().equals(endTime.toLocalDate())) ? endTime.toLocalTime() : LocalTime.MAX;
      outputs.add(TimeFormat.DEFAULT_DAY.of(s) + " - " + new DayEntry(start, end));
      s = s.plusDays(1);
    }
    return String.join("\n", outputs);
  }

  public String toDayString() {
    LocalDateTime s = startTime;
    final List<String> outputs = new ArrayList<>();
    while (s.isBefore(endTime)) {
      final LocalTime start = (s.toLocalDate().equals(startTime.toLocalDate())) ? startTime.toLocalTime() : LocalTime.MIN;
      final LocalTime end = (s.toLocalDate().equals(endTime.toLocalDate())) ? endTime.toLocalTime() : LocalTime.MAX;
      outputs.add(new DayEntry(start, end).toString());
      s = s.plusDays(1);
    }
    return String.join("\n", outputs);
  }

  @AllArgsConstructor
  public static class DayEntry {
    private LocalTime start;
    private LocalTime end;

    @Override
    public String toString() {
      if (start.equals(LocalTime.MIN)) {
        if (end.equals(LocalTime.MAX)) return "ganzer Tag";
        return "bis " + (end.getMinute() == 0 ? end.getHour() + " Uhr" : TimeFormat.HOUR.of(end) + "Uhr");
      }
      if (end.equals(LocalTime.MAX)) return "ab " + (start.getMinute() == 0 ? start.getHour() + " Uhr" : TimeFormat.HOUR.of(start) + "Uhr");
      if (start.getMinute() == 0 && end.getMinute() == 0) return "von " + start.getHour() + " bis " + end.getHour() + " Uhr";
      return "von " + TimeFormat.HOUR.of(start) + " bis " + TimeFormat.HOUR.of(end) + " Uhr";
    }
  }


}
