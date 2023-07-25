package de.xahrie.trues.api.datatypes.calendar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import de.xahrie.trues.api.coverage.playday.config.PlaydayConfig;
import de.xahrie.trues.api.coverage.stage.model.PlayStage;
import de.xahrie.trues.api.coverage.stage.model.Stage;
import de.xahrie.trues.api.coverage.playday.RepeatType;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@Data
@RequiredArgsConstructor
@ExtensionMethod(DateTimeUtils.class)
public final class WeekdayTimeRange {
  private final WeekdayTime start;
  private final WeekdayTime end;
  private final int additionalWeeks;

  public WeekdayTimeRange(DayOfWeek day, LocalTime startTime, int minutes) {
    this(new WeekdayTime(day, startTime), new WeekdayTime(day, startTime.plusMinutes(minutes)), 0);
  }

  public WeekdayTimeRange(WeekdayTime start, WeekdayTime end) {
    this(start, end, 0);
  }

  public TimeRange nextOrCurrent(Stage stage, int index) {
    return nextOrCurrent(determineDateOfWeekday(stage, index));
  }

  private TimeRange nextOrCurrent(LocalDate date) {
    final LocalDateTime startDateTime = start.nextOrCurrent(date);
    final LocalDateTime endDateTime = end.nextOrCurrent(startDateTime.toLocalDate())
        .plusWeeks(additionalWeeks);
    return new TimeRange(startDateTime, endDateTime);
  }

  private LocalDate determineDateOfWeekday(Stage stage, int index) {
    final PlaydayConfig config = ((PlayStage) stage).playdayConfig();
    LocalDateTime startTime = stage.getRange().getStartTime();
    if (config.customDays() == null) {
      final RepeatType repeatType = config.repeater().type();
      startTime = startTime.plusDays((long) repeatType.getDays() * (index - 1));
    }
    return startTime.toLocalDate();
  }

}
