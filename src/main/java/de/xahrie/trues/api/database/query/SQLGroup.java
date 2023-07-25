package de.xahrie.trues.api.database.query;

import lombok.Getter;

@Getter
public final class SQLGroup extends SQLOrder {
  private String having;

  public SQLGroup(String columnName) {
    super(columnName);
  }

  public SQLGroup having(String condition) {
    this.having = condition;
    return this;
  }

  @Override
  public String toString() {
    return super.toString() + (having == null ? "" : " HAVING " + having);
  }
}
