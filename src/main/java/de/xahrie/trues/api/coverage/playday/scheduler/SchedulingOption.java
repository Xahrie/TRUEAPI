package de.xahrie.trues.api.coverage.playday.scheduler;

import de.xahrie.trues.api.coverage.playday.config.DivisionRange;
import de.xahrie.trues.api.datatypes.calendar.WeekdayTime;
import de.xahrie.trues.api.datatypes.calendar.WeekdayTimeRange;
import lombok.Builder;

public record SchedulingOption(DivisionRange divisionRange, WeekdayTime defaultTime, WeekdayTimeRange range) {

  @Builder
  @SuppressWarnings("unused")
  public SchedulingOption {  }
}
