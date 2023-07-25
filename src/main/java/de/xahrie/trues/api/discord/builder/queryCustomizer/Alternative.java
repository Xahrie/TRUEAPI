package de.xahrie.trues.api.discord.builder.queryCustomizer;

import java.util.List;

public record Alternative(int index, List<String> names, boolean ignoreIfEmpty) {
  public Alternative(int index, List<String> names) {
    this(index, names, false);
  }
}
