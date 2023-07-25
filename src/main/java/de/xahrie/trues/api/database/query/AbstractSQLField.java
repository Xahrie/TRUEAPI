package de.xahrie.trues.api.database.query;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public class AbstractSQLField {
  private final String columnName;

  @Override
  public String toString() {
    return this instanceof Condition condition ? condition.toString() : getColumnName() + " = ?";
  }
}
