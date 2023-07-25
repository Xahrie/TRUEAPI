package de.xahrie.trues.api.calendar.scheduling;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import de.xahrie.trues.api.util.StringUtils;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public record DateStringConverter(String input) {
  @NonNull
  List<LocalDate> toList() {
    if (input.contains("-")) {
      final String[] splitted = input.split("-");
      if (splitted.length < 2) return new DateStringConverter(input.replace("-", "")).toList();

      final String startDay = splitted[0];
      final String endDay = splitted[1];
      return handleDateRanges(startDay, endDay);
    } else {
      final LocalDate localDate = handleDateString(input);
      return localDate == null ? List.of() : List.of(localDate);
    }
  }

  private List<LocalDate> handleDateRanges(String startDay, String endDay) {
    final LocalDate startDate = handleDateString(startDay);
    final LocalDate endDate = handleDateString(endDay);
    if (startDate == null || endDate == null) return List.of();

    final long additionalDays = Duration.between(startDate.atStartOfDay(), endDate.atStartOfDay()).toDays();
    return IntStream.iterate(0, i -> i < additionalDays + 1, i -> i + 1)
        .mapToObj(i -> startDate.plus(i, ChronoUnit.DAYS)).toList();
  }

  @Nullable
  private LocalDate handleDateString(String section) {
    if (StringUtils.erase(section, 1).contains(".")) {
      if (section.endsWith(".")) section = StringUtils.erase(section, 1);
      if (StringUtils.intValue(section.replace(".", "")) == -1) return null;
      final String[] splitted = section.split("\\.");
      int day = StringUtils.intValue(section);
      int month = LocalDate.now().getMonthValue();
      int year = LocalDate.now().getYear();

      if (splitted.length > 0) {
        day = StringUtils.intValue(splitted[0]);
        month = StringUtils.intValue(splitted[1]);
        if (splitted.length > 2) year = StringUtils.intValue(splitted[2]);
      }
      try {
        return LocalDate.of(year, month, day);
      } catch (DateTimeException ignored) {
        return null;
      }
    }

    final DayOfWeek dayOfWeek = determineDayOfWeek(section);
    if (dayOfWeek == null) return null;

    final int repeated = StringUtils.intValue(StringUtils.after(section, "+"), section.contains("+") ? 1 : 0);
    final LocalDate date = LocalDate.now().with(TemporalAdjusters.nextOrSame(dayOfWeek));
    return date.plusWeeks(repeated);
  }

  @Nullable
  static DayOfWeek determineDayOfWeek(String day) {
    day = StringUtils.capitalizeFirst(day);
    try {
      final TemporalAccessor temporalAccessor = DateTimeFormatter.ofPattern("E").withLocale(Locale.GERMANY).parse(day + ".");
      return DayOfWeek.from(temporalAccessor);
    } catch (DateTimeParseException ignored) {
      return null;
    }
  }
}
