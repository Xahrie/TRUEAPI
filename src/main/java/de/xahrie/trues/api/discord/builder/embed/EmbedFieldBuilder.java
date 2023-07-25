package de.xahrie.trues.api.discord.builder.embed;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class EmbedFieldBuilder<T> {
  private final List<MessageEmbed.Field> fields;
  private final List<T> data;

  public EmbedFieldBuilder(List<T> data) {
    this(new ArrayList<>(), data);
  }

  public EmbedFieldBuilder<T> outline(String key, String value) {
    return handleAddField(key, value, false);
  }

  public EmbedFieldBuilder<T> num(String key, Function<T, String> consumer) {
    final StringJoiner joiner = new StringJoiner("\n");
    IntStream.range(0, data.size()).mapToObj(i -> i + 1 + ". " + consumer.apply(data.get(i))).forEach(joiner::add);
    final String content = joiner.toString();
    return handleAddField(key, content, true);
  }

  public EmbedFieldBuilder<T> add(String key, Function<T, String> consumer) {
    final String content = data.stream().map(consumer).collect(Collectors.joining("\n"));
    return handleAddField(key, content, true);
  }

  @NotNull
  private EmbedFieldBuilder<T> handleAddField(String key, String content, boolean inline) {
    if (content.isBlank()) content = "keine Daten";
    final var field = new MessageEmbed.Field(key, content.substring(0, Math.min(1024, content.length())), inline);
    fields.add(field);
    return this;
  }

  public List<MessageEmbed.Field> build() {
    return fields;
  }
}
