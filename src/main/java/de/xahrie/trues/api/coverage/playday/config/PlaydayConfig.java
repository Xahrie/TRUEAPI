package de.xahrie.trues.api.coverage.playday.config;

import java.util.List;

import de.xahrie.trues.api.coverage.playday.scheduler.SchedulingOption;
import de.xahrie.trues.api.coverage.match.model.MatchFormat;
import de.xahrie.trues.api.coverage.stage.model.Stage;
import de.xahrie.trues.api.datatypes.calendar.WeekdayTimeRange;
import lombok.Builder;

public record PlaydayConfig(Stage.StageType stageType, MatchFormat format, WeekdayTimeRange dayRange, List<SchedulingOption> options,
                            List<WeekdayTimeRange> customDays, TimeRepeater repeater) {
  @Builder
  @SuppressWarnings("unused")
  public PlaydayConfig {  }

  public WeekdayTimeRange playdayRange(int index) {
    return customDays() == null ? dayRange() : customDays().get(index - 1);
  }

}
