package de.xahrie.trues.api.coverage.match.model;

import de.xahrie.trues.api.datatypes.calendar.TimeRange;

public interface AScheduleable {
  TimeRange getRange(); // scheduling_start, scheduling_end

  void setRange(TimeRange range);
}
