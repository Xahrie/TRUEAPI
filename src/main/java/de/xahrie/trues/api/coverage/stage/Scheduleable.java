package de.xahrie.trues.api.coverage.stage;

import de.xahrie.trues.api.datatypes.calendar.TimeRange;

public interface Scheduleable {
  TimeRange getRange(); // scheduling_start, scheduling_end
  void setRange(TimeRange range);
  boolean isScheduleable();
}
