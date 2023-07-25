package de.xahrie.trues.api.database.query;

import de.xahrie.trues.api.database.connector.Table;
import lombok.Getter;

@Getter
public final class SQLReturnField extends SQLField {
  private final boolean distinct;
  public static SQLReturnField idOf(Class<Id> idClass) {
    final String tableName = idClass.getAnnotation(Table.class).value();
    return new SQLReturnField(tableName + "_id", Integer.class);
  }
  public SQLReturnField(String columnName, Class<?> clazz) {
    super(columnName, clazz);
    this.distinct = false;
  }

  public SQLReturnField(String columnName, Class<?> clazz, boolean distinct) {
    super(columnName, clazz);
    this.distinct = distinct;
  }

  public Class<?> getReturnType() {
    return (Class<?>) super.getValue();
  }

  @Override
  public Object getValue() {
    try {
      throw new NoSuchMethodException("Nutze getReturnType");
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toString() {
    return super.toString();
  }
}
