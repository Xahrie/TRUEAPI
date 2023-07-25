package de.xahrie.trues.api.coverage.playday.scheduler;

import java.util.List;

import de.xahrie.trues.api.coverage.league.model.LeagueTier;
import de.xahrie.trues.api.datatypes.calendar.WeekdayTime;
import de.xahrie.trues.api.datatypes.calendar.WeekdayTimeRange;

public class Scheduling {
  private final List<SchedulingOption> options;

  public Scheduling(List<SchedulingOption> options) {
    this.options = options;
  }

  public WeekdayTimeRange range(LeagueTier tier) {
    return options.stream().filter(option -> option.divisionRange().isInside(tier))
        .map(SchedulingOption::range).findFirst().orElse(null);
  }

  public WeekdayTime defaultTime(LeagueTier tier) {
    return options.stream().filter(option -> option.divisionRange().isInside(tier))
        .map(SchedulingOption::defaultTime).findFirst().orElse(null);
  }

}
