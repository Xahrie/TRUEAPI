package de.xahrie.trues.api.database.query;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
@Getter
public abstract class SQLField extends AbstractSQLField {
  protected static SQLField.Key key(String columnName, Object value) {
    return new Key(columnName, value);
  }

  protected static SQLField.Updateable set(String columnName, Object value) {
    return new Updateable(columnName, value);
  }

  public static SQLReturnField get(String columnName, Class<?> clazz) {
    return new SQLReturnField(columnName, clazz);
  }

  public static SQLReturnField distinct(String columnName, Class<?> clazz) {
    return new SQLReturnField(columnName, clazz, true);
  }

  private final Object value;

  public SQLField(String columnName, Object value) {
    super(columnName);
    this.value = value;
  }

  @EqualsAndHashCode(callSuper = true)
  @Getter
  static final class Updateable extends SQLField {
    public Updateable(String columnName, Object value) {
      super(columnName, value);
    }
  }

  @EqualsAndHashCode(callSuper = true)
  @Getter
  public static final class Key extends SQLField {
    public Key(String columnName, Object value) {
      super(columnName, value);
    }
  }
}
