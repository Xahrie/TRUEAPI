package de.xahrie.trues.api.discord.builder.string;

import java.util.List;
import java.util.stream.IntStream;

import de.xahrie.trues.api.discord.builder.queryCustomizer.SimpleCustomQuery;

public final class StringQueryBuilder {
  private final StringCreator creator;
  private final SimpleCustomQuery query;
  private int index;

  public StringQueryBuilder(StringCreator creator, SimpleCustomQuery query, int index) {
    this.creator = creator;
    this.query = query;
    this.index = index;
  }

  public StringCreator build(Integer colIndex, String name) {
    final List<Object[]> entries = query.build();
    if (!entries.isEmpty()) {
      final var dataHandler = new StringDataHandler(query, entries);
      creator.add(dataHandler.createHeader(index, colIndex, name));
      IntStream.range(0, entries.size()).forEach(i -> {
        creator.add(dataHandler.create(i, index + 1));
        index++;
      });
    }
    return creator;
  }
}
