package de.xahrie.trues.api.discord.command.slash;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Align {
  LEFT("-"),
  RIGHT(""),
  AUTO(null);
  private final String sign;
}
