package de.xahrie.trues.api.coverage.stage.model;

import java.io.Serial;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import de.xahrie.trues.api.coverage.playday.config.PlaydayConfig;
import de.xahrie.trues.api.coverage.match.model.MatchFormat;
import de.xahrie.trues.api.coverage.season.OrgaCupSeason;
import de.xahrie.trues.api.coverage.season.Season;
import de.xahrie.trues.api.coverage.stage.Scheduleable;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;
import de.xahrie.trues.api.datatypes.calendar.WeekdayTimeRange;
import de.xahrie.trues.api.util.Util;

@Table(value = "coverage_stage", department = "Playoffs")
public class PlayoffStage extends Stage implements Entity<PlayoffStage>, PlayStage, Scheduleable {
  @Serial
  private static final long serialVersionUID = -381890929196558760L;

  public PlayoffStage(Season season, TimeRange range) {
    super(season, range);
  }

  private PlayoffStage(int id, int seasonId, TimeRange range, Long discordEventId) {
    super(id, seasonId, range, discordEventId);
  }

  @Override
  public Integer pageId() {
    return Util.avoidNull(StageType.fromClass(getClass()), null, StageType::getPrmId);
  }

  @Override
  public boolean isScheduleable() {
    return getSeason() instanceof OrgaCupSeason;
  }

  @Override
  public PlaydayConfig playdayConfig() {
    return PlaydayConfig.builder()
        .stageType(StageType.PLAYOFF_STAGE)
        .format(MatchFormat.BEST_OF_THREE)
        .customDays(List.of(
            new WeekdayTimeRange(DayOfWeek.SATURDAY, LocalTime.of(14, 0), 139),
            new WeekdayTimeRange(DayOfWeek.SATURDAY, LocalTime.of(18, 0), 139),
            new WeekdayTimeRange(DayOfWeek.SUNDAY, LocalTime.of(14, 0), 139),
            new WeekdayTimeRange(DayOfWeek.SUNDAY, LocalTime.of(18, 0), 139),
            new WeekdayTimeRange(DayOfWeek.MONDAY, LocalTime.of(20, 0), 139)
        )).build();
  }

  public static PlayoffStage get(List<Object> objects) {
    return new PlayoffStage(
        (int) objects.get(0),
        (int) objects.get(2),
        new TimeRange((LocalDateTime) objects.get(3), (LocalDateTime) objects.get(4)),
        (Long) objects.get(5)
    );
  }

  @Override
  public PlayoffStage create() {
    return new Query<>(PlayoffStage.class).key("season", seasonId)
        .col("stage_start", range.getStartTime()).col("stage_end", range.getEndTime()).col("discord_event", discordEventId)
        .insert(this);
  }

}
