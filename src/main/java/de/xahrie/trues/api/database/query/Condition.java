package de.xahrie.trues.api.database.query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Getter
public final class Condition extends AbstractSQLField {
  public static Condition isNull(String columnName) {
    return new Condition(columnName + " is null");
  }
  public static Condition notNull(String columnName) {
    return new Condition(columnName + " is not null");
  }

  public static Condition between(String columnName, Object value, Object value2) {
    if (value instanceof String string1) {
      if (value2 instanceof String string2) return new Condition(columnName + " between " + string1 + " and " + string2);
      return new Condition(columnName + " between " + string1 + " and ?", List.of(value2));
    }
    return new Condition(columnName + " between ? and ?", List.of(value, value2));
  }

  static Condition compare(Comparer comparer, String columnName, Object value) {
    final ArrayList<Object> objects = new ArrayList<>();
    objects.add(value);
    return new Condition(columnName + comparer.operant + "?", objects);
  }

  public static Condition inList(String columnName, List<Object> values) {
    if (values.isEmpty()) return new Condition("0");
    final String amount = IntStream.range(0, values.size()).mapToObj(i -> "?").collect(Collectors.joining(", "));
    return new Condition(columnName + " in (" + amount + ")", values);
  }

  public static Condition notInList(String columnName, List<Object> values) {
    if (values.isEmpty()) return new Condition("1");
    final String amount = IntStream.range(0, values.size()).mapToObj(i -> "?").collect(Collectors.joining(", "));
    return new Condition(columnName + " not in (" + amount + ")", values);
  }

  public static Condition inSubquery(String columnName, Query<?> query) {
    if (query.getInnerSimpleQuery().isBlank()) return new Condition("0");
    return new Condition(columnName + " in (" + query.getInnerSimpleQuery() + ")", query.getValues(query.getInnerSimpleQuery()));
  }

  public static Condition notInSubquery(String columnName, Query<?> query) {
    if (query.getInnerSimpleQuery().isBlank()) return new Condition("1");
    return new Condition(columnName + " not in (" + query.getInnerSimpleQuery() + ")", query.getValues(query.getInnerSimpleQuery()));
  }

  private final List<Object> paramsToAdd;

  public Condition(String columnName) {
    this(columnName, List.of());
  }

  public Condition(String columnName, List<Object> paramsToAdd) {
    super(columnName);
    this.paramsToAdd = paramsToAdd;
  }

  public Condition merge(Condition condition, boolean and) {
    if (getColumnName().equals("0") || condition.getColumnName().equals("0")) return new Condition("0");
    if (getColumnName().equals("1")) return condition;
    if (condition.getColumnName().equals("1")) return this;
    final ArrayList<Object> newParams = new ArrayList<>(getParamsToAdd());
    newParams.addAll(condition.getParamsToAdd());
    return new Condition(getColumnName() + (and ? " and " : " or ") + condition.getColumnName(), newParams);
  }


  @Override
  public String toString() {
    return super.getColumnName();
  }

  @RequiredArgsConstructor
  public enum Comparer {
    EQUAL(" = "),
    LIKE(" LIKE "),
    GREATER_THAN(" > "),
    SMALLER_THAN(" < "),
    GREATER_EQUAL(" >= "),
    SMALLER_EQUAL(" <= "),
    NOT_EQUAL(" <> ");
    private final String operant;
  }
}
