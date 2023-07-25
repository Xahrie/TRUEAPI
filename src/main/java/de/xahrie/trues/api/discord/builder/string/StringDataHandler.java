package de.xahrie.trues.api.discord.builder.string;

import java.util.ArrayList;
import java.util.List;

import de.xahrie.trues.api.discord.builder.queryCustomizer.Enumeration;
import de.xahrie.trues.api.discord.builder.queryCustomizer.SimpleCustomQuery;
import de.xahrie.trues.api.discord.command.slash.Align;
import de.xahrie.trues.api.discord.command.slash.Column;
import de.xahrie.trues.api.database.connector.CellEntry;
import de.xahrie.trues.api.util.Util;
import org.jetbrains.annotations.NotNull;

public class StringDataHandler {
  private final SimpleCustomQuery query;
  private final List<Object[]> entries;
  private final List<ColumnData> lengths;
  private int index;

  public StringDataHandler(SimpleCustomQuery query, List<Object[]> entries) {
    this.query = query;
    this.entries = entries;
    this.lengths = determineLengths();
  }

  @NotNull
  private List<ColumnData> determineLengths() {
    final List<ColumnData> lengths = new ArrayList<>();
    for (int j = 0; j < entries.get(0).length; j++) {
      final int i = j;
      final Column column = query.getColumns().get(i);
      final int maxRowLength = Math.min(Util.avoidNull(column.getMaxLength(), Integer.MAX_VALUE),
          entries.stream().map(object -> new CellEntry(object[i], column).getSignedLength())
              .max(Integer::compare).orElse(column.getMaxLength()));
      if (column.getDelimiter() != null) {
        final int lastIndex = lengths.size() - 1;
        lengths.get(lastIndex).add(maxRowLength, column.getDelimiter());
        continue;
      }
      lengths.add(new ColumnData(j, maxRowLength));
    }
    return lengths;
  }

  public String createHeader(int index, Integer colIndex, String name) {
    this.index = index;
    final Object[] head = query.getColumns().stream().map(Column::getName).toArray();
    if (name != null) head[colIndex] = name;
    this.index--;
    final StringBuilder headString = new StringBuilder(createHead(head) + "\n");
    headString.append(handleEnumeration(null, ""));
    for (int i = 0; i < lengths.size(); i++) {
      if (i > 0) headString.append("+");
      headString.append("-".repeat(lengths.get(i).getLength() + (i == 0 || i == lengths.size() - 1 ? 1 : 2)));
    }
    return headString.toString();
  }

  public String create(int entryIndex, int index) {
    this.index = index;
    final Object[] entry = entries.get(entryIndex);
    return create(entry, query.getEnumeration());
  }

  private String createHead(Object[] entry) {
    final List<String> keys = new ArrayList<>();
    for (ColumnData wrapper : lengths) {
      final int index = wrapper.getIndex();
      final Column column = query.getColumns().get(index);
      final var baseEntry = new CellEntry(entry[index], column);
      if (column.getDelimiter() != null) continue;

      keys.add(String.format("%-" + wrapper.getLength() + "s", baseEntry));
    }
    final String data = String.join(" | ", keys);
    return handleEnumeration(null, data);
  }

  private String create(Object[] entry, @NotNull
  Enumeration enumeration) {
    final List<String> keys = new ArrayList<>();
    for (ColumnData wrapper : lengths) {
      final int index = wrapper.getIndex();
      Column column = query.getColumns().get(index);
      final var baseEntry = new CellEntry(entry[index], column);
      String cell = Util.avoidNull(column.getDelimiter(), "") + format(enumeration, wrapper, 0, baseEntry);

      if (wrapper.isTwoInOne()) {
        column = query.getColumns().get(index + 1);
        final var additional = new CellEntry(entry[index + 1], column);
        final String cell2 = Util.avoidNull(column.getDelimiter(), "") + format(enumeration, wrapper, 1, additional);
        cell += cell2;
      }
      keys.add(cell);
    }
    final String data = String.join(" | ", keys);
    return handleEnumeration(enumeration, data);
  }

  private String format(Enumeration enumeration, ColumnData wrapper, int index, CellEntry entry) {
    Align align = entry.column().getAlign();

    if (align.equals(Align.AUTO)) align = entry.entry() instanceof String ? Align.LEFT : Align.RIGHT;
    final int length = (enumeration.equals(Enumeration.NONE) ? wrapper.getLength() : wrapper.getSubLengths().get(index))
        - (entry.column().isSigned() ? 1 : 0);
    final String format = "%" + align.getSign() + length + "s";
    return (entry.isSigned() ? "+" : "") + String.format(format, entry.round());
  }

  private String handleEnumeration(Enumeration enumeration, String data) {
    final int leadingSpaces = String.valueOf(entries.size()).length() - String.valueOf(index).length();
    if (enumeration != null && !enumeration.equals(Enumeration.NONE)) data = " ".repeat(leadingSpaces) + index + ". " + data;
    else if (!query.getEnumeration().equals(Enumeration.NONE)) data = " ".repeat(String.valueOf(entries.size()).length() + 2) + data;
    this.index++;
    return data;
  }

}
