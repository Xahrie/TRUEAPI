package de.xahrie.trues.api.coverage.playday.scheduler;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import de.xahrie.trues.api.coverage.playday.config.PlaydayRange;
import de.xahrie.trues.api.coverage.playday.config.SchedulingRange;
import de.xahrie.trues.api.coverage.league.model.LeagueTier;
import de.xahrie.trues.api.coverage.playday.config.PlaydayConfig;
import de.xahrie.trues.api.coverage.stage.model.PlayStage;
import de.xahrie.trues.api.coverage.stage.model.Stage;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;
import de.xahrie.trues.api.datatypes.calendar.WeekdayTime;
import de.xahrie.trues.api.datatypes.calendar.WeekdayTimeRange;

public record PlaydayScheduler(PlaydayRange playday, LocalDateTime defaultTime, SchedulingRange scheduling) {
  public static PlaydayScheduler create(Stage stage, int index, LeagueTier tier) {
    final PlaydayConfig config = ((PlayStage) stage).playdayConfig();

    final TimeRange playdayTimeRange = config.playdayRange(index).nextOrCurrent(stage, index);
    final var playdayRange = new PlaydayRange(playdayTimeRange.getStartTime(), playdayTimeRange.getEndTime());

    final var scheduling = new Scheduling(config.options());
    WeekdayTimeRange range = scheduling.range(tier);
    if (range == null) {
      range = new WeekdayTimeRange(new WeekdayTime(DayOfWeek.SUNDAY, LocalTime.of(14, 0)),
        new WeekdayTime(DayOfWeek.SUNDAY,
          LocalTime.of(18, 0)));
    }
    final var schedulingTimeRange = range.nextOrCurrent(stage, index);
    final var schedulingRange = new SchedulingRange(schedulingTimeRange.getStartTime(), schedulingTimeRange.getEndTime());

    final WeekdayTime timeOffset = scheduling.defaultTime(tier);
    final LocalDateTime defaultTime = timeOffset.nextOrCurrent(playdayTimeRange.getStartTime());
    return new PlaydayScheduler(playdayRange, defaultTime, schedulingRange);
  }
}
