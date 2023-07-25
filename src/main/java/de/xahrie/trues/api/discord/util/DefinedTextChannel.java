package de.xahrie.trues.api.discord.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum DefinedTextChannel {
  ADMIN_CHANNEL(1008997272467546112L),
  ADMIN_INTERN(1118121503419027536L),
  BEWERBER(1093826861152358480L),
  RANKED(948116005589221378L);
  private final long id;
}
