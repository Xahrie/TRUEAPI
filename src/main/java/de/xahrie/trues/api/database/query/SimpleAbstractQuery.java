package de.xahrie.trues.api.database.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.xahrie.trues.api.database.connector.Table;
import org.jetbrains.annotations.Nullable;

public abstract class SimpleAbstractQuery<T extends Id> {
  protected final Class<T> targetId;
  protected Class<? extends Id> subEntity;
  protected final List<SQLField> fields = new ArrayList<>();
  protected final List<JoinQuery<?, ?>> joins = new ArrayList<>();
  protected final ConditionManager conditionManager;
  protected SQLGroup group;
  protected SQLOrder order;
  protected Integer offset;
  protected int limit = 1000;
  protected final List<Union> unified = new ArrayList<>();
  protected List<Object> additionalParameters = new ArrayList<>();

  public SimpleAbstractQuery(Class<T> targetId) {
    this.targetId = targetId;
    this.conditionManager = new ConditionManager(targetId);
  }

  public List<Object> getParameters() {
    final ArrayList<Object> objects = new ArrayList<>(additionalParameters);
    objects.addAll(conditionManager.getValues());
    return objects;
  }

  public Query<T> key(String key, Object value) {
    return field(new SQLField.Key(key, value));
  }

  public Query<T> col(String key, Object value) {
    return field(SQLField.set(key, value));
  }

  protected Query<T> getAll(Class<? extends Id> subEntity) {
    this.subEntity = subEntity;
    return (Query<T>) this;
  }

  public Query<T> get(String columnName, Class<?> clazz) {
    return field(SQLField.get(columnName, clazz));
  }

  public Query<T> distinct(String columnName, Class<?> clazz) {
    return field(SQLField.distinct(columnName, clazz));
  }

  public Query<T> get(@Nullable String delimiter, Formatter... columnNames) {
    final String joinDelimiter = delimiter == null ? ", " : ", '" + delimiter + "', ";
    final String columnName = Arrays.stream(columnNames)
        .map(formatter -> formatter.toString("_" + targetId.getSimpleName().toLowerCase().replace("abstract", "")))
        .collect(Collectors.joining(joinDelimiter, "CONCAT(", ")"));
    return get(columnName, String.class);
  }

  public Query<T> field(SQLField sqlField) {
    fields.add(sqlField);
    return (Query<T>) this;
  }

  public <E extends Id, J extends Id> Query<T> join(Class<E> valueClass, Class<J> targetClass) {
    return join(new JoinQuery<>(valueClass, targetClass));
  }

  public <E extends Id, J extends Id> Query<T> join(JoinQuery<E, J> joinSimpleQuery) {
    joins.add(joinSimpleQuery);
    additionalParameters.addAll(joinSimpleQuery.getParams());
    final Query<E> innerQuery = joinSimpleQuery.getInnerQuery();
    if (innerQuery != null) {
      final String selectString = joinSimpleQuery.getTargetClass() == null ? innerQuery.query : innerQuery.getSelectString(false);
      additionalParameters.addAll(innerQuery.getValues(selectString));
    }
    return (Query<T>) this;
  }

  public Query<T> forId(int id) {
    return or(getTableName() + "_id", id);
  }

  public Query<T> where(Condition... conditions) {
    Arrays.stream(conditions).forEach(conditionManager::and);
    return (Query<T>) this;
  }

  public Query<T> where(String name, Object value) {
    conditionManager.and(Condition.compare(Condition.Comparer.EQUAL, name, value));
    return (Query<T>) this;
  }

  public Query<T> where(String query) {
    conditionManager.and(new Condition(query));
    return (Query<T>) this;
  }

  public Query<T> where(Condition.Comparer comparer, String name, Object value) {
    conditionManager.and(Condition.compare(comparer, name, value));
    return (Query<T>) this;
  }

  public Query<T> or(Condition condition) {
    conditionManager.or(condition);
    return (Query<T>) this;
  }

  public Query<T> or(String name, Object value) {
    conditionManager.or(Condition.compare(Condition.Comparer.EQUAL, name, value));
    return (Query<T>) this;
  }

  public Query<T> or(String query) {
    conditionManager.or(new Condition(query));
    return (Query<T>) this;
  }

  public Query<T> or(Condition.Comparer comparer, String name, Object value) {
    conditionManager.or(Condition.compare(comparer, name, value));
    return (Query<T>) this;
  }

  public Query<T> keep(Condition.Comparer comparer, String name, Object value) {
    conditionManager.keep(Condition.compare(comparer, name, value));
    return (Query<T>) this;
  }

  public Query<T> keep(String name, Object value) {
    conditionManager.keep(Condition.compare(Condition.Comparer.EQUAL, name, value));
    return (Query<T>) this;
  }

  public Query<T> and(String query) {
    conditionManager.and(new Condition(query));
    return (Query<T>) this;
  }

  public Query<T> and(Condition condition) {
    conditionManager.and(condition);
    return (Query<T>) this;
  }

  public Query<T> and(String name, Object value) {
    conditionManager.and(Condition.compare(Condition.Comparer.EQUAL, name, value));
    return (Query<T>) this;
  }

  public Query<T> and(Condition.Comparer comparer, String name, Object value) {
    conditionManager.and(Condition.compare(comparer, name, value));
    return (Query<T>) this;
  }

  public Query<T> groupBy(SQLGroup group) {
    this.group = group;
    return (Query<T>) this;
  }

  public Query<T> groupBy(String name) {
    this.group = new SQLGroup(name);
    return (Query<T>) this;
  }

  public Query<T> ascending(String column) {
    return ascending(column, true);
  }

  public Query<T> ascending(String column, boolean nullsFirst) {
    this.order = new SQLOrder(column, false, nullsFirst);
    return (Query<T>) this;
  }

  public Query<T> descending(String column) {
    return descending(column, true);
  }

  public Query<T> descending(String column, boolean nullsFirst) {
    this.order = new SQLOrder(column, true, nullsFirst);
    return (Query<T>) this;
  }

  public Query<T> offset(int offset) {
    this.offset = offset;
    return (Query<T>) this;
  }

  Query<T> limit(int limit) {
    this.limit = limit;
    return (Query<T>) this;
  }

  public Query<T> include(Query<?> query) {
    unified.add(new Union(query, Union.UnionType.UNION));
    return (Query<T>) this;
  }

  public Query<T> exclude(Query<?> query) {
    unified.add(new Union(query, Union.UnionType.EXCEPT));
    return (Query<T>) this;
  }

  protected String getTableName() {
    return targetId.getAnnotation(Table.class).value();
  }

  protected String getDepartment() {
    if (targetId == null) return null;
    final Table annotation = targetId.getAnnotation(Table.class);
    if (annotation == null) return "";
    final String department = annotation.department();
    return department.isBlank() ? null : department;
  }
}
