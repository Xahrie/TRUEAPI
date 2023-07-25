package de.xahrie.trues.api.discord.builder.string;

import java.util.ArrayList;
import java.util.List;

import de.xahrie.trues.api.util.Util;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class ColumnData {
  private final int index;
  private final List<Integer> subLengths = new ArrayList<>();
  private String delimiter = null;

  public ColumnData(int index, int length) {
    this.index = index;
    this.subLengths.add(length);
  }

  public void add(int length, String delimiter) {
    subLengths.add(length);
    this.delimiter = delimiter;
  }

  public boolean isTwoInOne() {
    return this.subLengths.size() > 1;
  }

  public int getLength() {
    final int length = subLengths.stream().mapToInt(Integer::intValue).sum();
    return this.subLengths.size() == 1 ? length : length + Util.avoidNull(delimiter, 0, String::length);
  }
}
