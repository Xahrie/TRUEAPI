package de.xahrie.trues.api.discord.builder;

import java.util.List;

import de.xahrie.trues.api.discord.builder.embed.EmbedCreator;
import de.xahrie.trues.api.discord.builder.embed.EmbedQueryBuilder;
import de.xahrie.trues.api.discord.builder.queryCustomizer.Alternative;
import de.xahrie.trues.api.discord.builder.queryCustomizer.SimpleCustomQuery;
import de.xahrie.trues.api.discord.builder.string.StringCreator;
import de.xahrie.trues.api.discord.builder.string.StringQueryBuilder;
import de.xahrie.trues.api.util.Util;

public record InfoPanelBuilder(String title, String description, List<SimpleCustomQuery> queries, Alternative alternative) {
  public EmbedWrapper build() {
    final Integer altIndex = Util.avoidNull(alternative, Alternative::index);
    int index = 0;
    EmbedWrapper wrapper = new EmbedWrapper();
    EmbedCreator currentEmbedCreator = null;
    StringCreator currentStringCreator = null;

    for (int i = 0; i < queries.size(); i++) {
      final int finalI = i;
      final Boolean ignore = Util.avoidNull(alternative, null, Alternative::ignoreIfEmpty);
      final String altName = Util.avoidNull(alternative, a -> a.names().get(finalI));
      final SimpleCustomQuery query = queries.get(i);

      if (query.getColumns().size() > 3) { // String handling
        if (currentStringCreator == null) currentStringCreator = new StringCreator(query.getEnumeration(), this.title, this.description, index);
        if (currentEmbedCreator != null) {
          wrapper = wrapper.embed(currentEmbedCreator.build());
          index = currentEmbedCreator.getIndex();
          currentEmbedCreator = null;
        }
        currentStringCreator = new StringQueryBuilder(currentStringCreator, query, index).build(altIndex, altName);

      } else { // Embed handling
        if (currentEmbedCreator == null) currentEmbedCreator = new EmbedCreator(query.getEnumeration(), this.title, this.description, index);
        if (currentStringCreator != null) {
          wrapper = wrapper.content(currentStringCreator.build());
          index = currentStringCreator.getIndex();
          currentStringCreator = null;
        }
        currentEmbedCreator = new EmbedQueryBuilder(currentEmbedCreator, query).build(altIndex, altName, ignore);
      }

      if (currentStringCreator != null) {
        wrapper = wrapper.content(currentStringCreator.build());
        currentStringCreator.clear();
      }
    }

    if (currentEmbedCreator != null) wrapper = wrapper.embed(currentEmbedCreator.build());
    if (currentStringCreator != null && !currentStringCreator.getData().isEmpty()) wrapper = wrapper.content(currentStringCreator.build());
    return wrapper;
  }
}
