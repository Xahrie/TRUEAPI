package de.xahrie.trues.api.coverage.stage.model;

import java.io.Serial;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import de.xahrie.trues.api.coverage.playday.config.PlaydayConfig;
import de.xahrie.trues.api.coverage.match.model.MatchFormat;
import de.xahrie.trues.api.coverage.season.Season;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;
import de.xahrie.trues.api.datatypes.calendar.WeekdayTimeRange;
import de.xahrie.trues.api.util.Util;

@Table(value = "coverage_stage", department = "Kalibrierungsphase")
public class CalibrationStage extends Stage implements Entity<CalibrationStage>, PlayStage {
  @Serial
  private static final long serialVersionUID = -5169288334446201150L;

  public CalibrationStage(Season season, TimeRange range) {
    super(season, range);
  }

  private CalibrationStage(int id, int seasonId, TimeRange range, Long discordEventId) {
    super(id, seasonId, range, discordEventId);
  }

  @Override
  public Integer pageId() {
    return Util.avoidNull(StageType.fromClass(getClass()), null, StageType::getPrmId);
  }

  @Override
  public PlaydayConfig playdayConfig() {
    return PlaydayConfig.builder()
        .stageType(StageType.CALIBRATION_STAGE)
        .format(MatchFormat.ONE_GAME)
        .customDays(List.of(
            new WeekdayTimeRange(DayOfWeek.SATURDAY, LocalTime.of(14, 0), 50),
            new WeekdayTimeRange(DayOfWeek.SATURDAY, LocalTime.of(15, 15), 50),
            new WeekdayTimeRange(DayOfWeek.SATURDAY, LocalTime.of(16, 30), 50),
            new WeekdayTimeRange(DayOfWeek.SATURDAY, LocalTime.of(17, 45), 50),
            new WeekdayTimeRange(DayOfWeek.SUNDAY, LocalTime.of(14, 0), 50),
            new WeekdayTimeRange(DayOfWeek.SUNDAY, LocalTime.of(15, 15), 50),
            new WeekdayTimeRange(DayOfWeek.SUNDAY, LocalTime.of(16, 30), 50),
            new WeekdayTimeRange(DayOfWeek.SUNDAY, LocalTime.of(17, 45), 50)
        )).build();
  }

  public static CalibrationStage get(List<Object> objects) {
    return new CalibrationStage(
        (int) objects.get(0),
        (int) objects.get(2),
        new TimeRange((LocalDateTime) objects.get(3), (LocalDateTime) objects.get(4)),
        (Long) objects.get(5)
    );
  }

  @Override
  public CalibrationStage create() {
    return new Query<>(CalibrationStage.class).key("season", seasonId)
        .col("stage_start", range.getStartTime()).col("stage_end", range.getEndTime()).col("discord_event", discordEventId)
        .insert(this);
  }
}
