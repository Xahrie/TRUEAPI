package de.xahrie.trues.api.calendar;

import de.xahrie.trues.api.datatypes.calendar.TimeRange;

public interface ACalendar {
  TimeRange getRange();
  void setRange(TimeRange range);
  String getDetails();
}
