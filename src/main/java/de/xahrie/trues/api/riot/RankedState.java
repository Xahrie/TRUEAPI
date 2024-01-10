package de.xahrie.trues.api.riot;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RankedState {
  MATCHES(true),
  RANKUPS(true),
  DAILY(false),
  TIER_RANKUPS(true),
  NONE(false);

  private final boolean userMessage;

  public boolean hasUserMessage() {
    return userMessage;
  }
}
