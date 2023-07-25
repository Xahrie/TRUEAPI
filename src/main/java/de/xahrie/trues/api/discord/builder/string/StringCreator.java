package de.xahrie.trues.api.discord.builder.string;

import java.util.ArrayList;
import java.util.List;

import de.xahrie.trues.api.discord.builder.queryCustomizer.Enumeration;
import de.xahrie.trues.api.util.Const;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class StringCreator {
  private final List<String> data = new ArrayList<>();
  private final Enumeration enumeration;
  private final String title;

  private String description;
  private int index;
  private int lengthRemaining = Const.DISCORD_MESSAGE_MAX_CHARACTERS;
  private final List<String> builders = new ArrayList<>();

  public StringCreator(Enumeration enumeration, String title, String description, int index) {
    this.enumeration = enumeration;
    this.title = title;
    this.description = description;
    this.index = index;
  }

  public void add(String row) {
    data.add(row);
  }

  public void clear() {
    data.clear();
    builders.clear();
  }

  public List<String> build() {
    this.lengthRemaining = Const.DISCORD_MESSAGE_MAX_CHARACTERS;
    StringBuilder builder = new StringBuilder();
    if (data.isEmpty()) return List.of("`keine Daten`");
    this.lengthRemaining -= data.get(0).length() + 2;
    builder.append("```").append(data.get(0));

    int entries = 0;
    while (lengthRemaining >= 3 && entries + 1 < data.size()) {
      final String stripped = data.get(entries + 1);
      this.lengthRemaining -= (stripped.length() + 2);
      entries++;
    }

    if (!enumeration.equals(Enumeration.NONE)) entries = entries / 5 * 5;

    for (int i = 0; i < data.size() / entries; i++) {
      final int start = i * entries + 1;
      final int end = Math.min(data.size(), start + entries);
      final String stripped = String.join("\n", data.subList(start, end));

      builders.add(builder + "```");
      builder = new StringBuilder("```" + stripped);
      if (end == data.size()) {
        builders.add(builder + "```");
        break;
      }
    }
    return new ArrayList<>(builders);
  }

}
