package de.xahrie.trues.api.datatypes.calendar;

import java.util.List;

public record TimeRanges(List<TimeRange> ranges) {
  public boolean matches(TimeRanges timeRanges) {
    if (ranges.size() != timeRanges.ranges().size()) return false;
    for (int i = 0; i < ranges.size(); i++) {
      final TimeRange range = ranges.get(i);
      final TimeRange otherRange = timeRanges.ranges.get(i);
      if (!range.getStartTime().toLocalTime().equals(otherRange.getStartTime().toLocalTime())) return false;
      if (!range.getEndTime().toLocalTime().equals(otherRange.getEndTime().toLocalTime())) return false;
    }
    return true;
  }
}
