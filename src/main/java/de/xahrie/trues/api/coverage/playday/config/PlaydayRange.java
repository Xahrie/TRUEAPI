package de.xahrie.trues.api.coverage.playday.config;

import java.time.LocalDateTime;

import de.xahrie.trues.api.datatypes.calendar.TimeRange;
import lombok.Getter;

@Getter
public class PlaydayRange extends TimeRange {
  public PlaydayRange(LocalDateTime start, LocalDateTime end) {
    super(start, end);
  }
}
