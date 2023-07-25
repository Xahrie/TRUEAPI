package de.xahrie.trues.api.coverage.playday;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RepeatType {
  DAILY(1),
  WEEKLY(7);

  private final int days;

}
