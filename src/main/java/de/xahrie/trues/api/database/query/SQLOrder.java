package de.xahrie.trues.api.database.query;

import de.xahrie.trues.api.util.StringUtils;
import lombok.Getter;
import lombok.experimental.ExtensionMethod;

@Getter
@ExtensionMethod(StringUtils.class)
public class SQLOrder extends AbstractSQLField {
  private final boolean descending;
  private final boolean nullsFirst;

  public SQLOrder(String columnName) {
    this(columnName, false, true);
  }

  public SQLOrder(String columnName, boolean descending, boolean nullsFirst) {
    super(columnName);
    this.descending = descending;
    this.nullsFirst = nullsFirst;
  }

  @Override
  public String toString() {
    if (getColumnName().contains("`") || getColumnName().toLowerCase().contains("count(") || getColumnName().toLowerCase().contains("avg(")) return getColumnName() + (descending ? " DESC" : "");
    final String name = getColumnName().contains(".") ? "`" + getColumnName().before(".") + "`.`" + getColumnName().after(".") + "`" : "`" + getColumnName() + "`";
    return (nullsFirst ? "" : "-") + name + (descending ? " DESC" : "");
  }
}
