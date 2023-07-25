package de.xahrie.trues.api.database.connector;

import java.util.List;
import java.util.stream.Collectors;

import de.xahrie.trues.api.util.StringUtils;

public interface DTO<T> extends Comparable<T> {
  List<Object> getData();

  default List<String> getStrings() {
    return getData().stream().map(Object::toString).collect(Collectors.toList());
  }

  default String getString(int index) {
    return getData().get(index).toString();
  }

  default Double getDouble(int index) {
    return StringUtils.doubleValue(getString(index), null);
  }

  default Integer getInt(int index) {
    return StringUtils.intValue(getString(index), null);
  }

  default Object get(int index) {
    final List<Object> data = getData();
    if (index >= data.size()) return "?";
    return data.get(index);
  }
}
