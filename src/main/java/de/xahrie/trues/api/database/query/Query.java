package de.xahrie.trues.api.database.query;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.UnknownFormatConversionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.mysql.cj.jdbc.exceptions.MysqlDataTruncation;
import de.xahrie.trues.api.database.connector.Database;
import de.xahrie.trues.api.database.connector.Listing;
import de.xahrie.trues.api.database.connector.SQLUtils;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.datatypes.collections.SortedList;
import de.xahrie.trues.api.util.Const;
import de.xahrie.trues.api.util.io.log.Console;
import de.xahrie.trues.api.util.io.log.DevInfo;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

@Log
public class Query<T extends Id> extends SimpleQueryFormer<T> {
  private static Map<Id, LocalDateTime> entities = Collections.synchronizedMap(new HashMap<>());
  private static int queryCount = 0, savedCount = 0, concurrentCount = 0;

  public static void remove(Id entity) {
    entities.remove(entity);
  }

  public static void reset() {
    entities = entities.entrySet().stream().filter(idLocalDateTimeEntry -> idLocalDateTimeEntry.getValue().plusDays(1).isAfter(LocalDateTime.now())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public Query(Class<T> clazz) {
    super(clazz);
  }

  public Query(Class<T> clazz, int amount) {
    super(clazz);
    limit(amount);
  }

  public Query(String query) {
    super(null, query);
    this.additionalParameters = List.of();
  }

  public Query(String query, List<Object> parameters) {
    super(null, query);
    this.additionalParameters = parameters;
  }

  public Query(Class<T> clazz, String query) {
    super(clazz, query);
  }

  public Query(Class<T> clazz, String query, List<Object> parameters) {
    super(clazz, query);
    this.additionalParameters = parameters;
  }

  public static int update(String query) {
    return update(query, List.of());
  }

  public static int update(String query, List<Object> parameters) {
    if (Const.SHOW_SQL) new Console(query).debug();
    try (final PreparedStatement statement = Database.connection().getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
      for (int i = 0; i < parameters.size(); i++)
        statement.setObject(i + 1, parameters.get(i));
      return statement.executeUpdate();
    } catch (MysqlDataTruncation exception) {
      String message = "Query nicht zulässig: " + query + "\nParameters: " +
                       parameters.stream().map(Object::toString)
                                 .collect(Collectors.joining(", "));
      if (!Const.SHOW_SQL) new Console(message).severe(exception);
      new DevInfo(message).with(Console.class).severe(exception);
      throw new IllegalArgumentException(exception);
    } catch (SQLException exception) {
      if (!Const.SHOW_SQL) new Console("Query nicht zulässig: " + query).severe(exception);
      new DevInfo("Query nicht zulässig: " + query).with(Console.class).severe(exception);
      throw new IllegalArgumentException(exception);
    }
  }

  /**
   * Füge Eintrag in die Datenbank ein
   *
   * @return Entity für Chaining
   */
  public T insert(T entity, List<Object> parameters) {
    return insert(entity, t -> null, t -> null, parameters);
  }

  public T insert(T entity) {
    return insert(entity, t -> null, t -> null, List.of());
  }

  /**
   * Füge Eintrag in die Datenbank ein
   *
   * @param action Wenn erfolgreich hinzugefügt (ohne Id)
   * @return Entity für Chaining
   */
  public <R> T insert(T entity, Function<T, R> action, List<Object> parameters) {
    return insert(entity, action, t -> null, parameters);
  }

  public <R> T insert(T entity, Function<T, R> action) {
    return insert(entity, action, t -> null, List.of());
  }

  /**
   * Füge Eintrag in die Datenbank ein
   *
   * @param action Wenn erfolgreich hinzugefügt (ohne Id)
   * @param otherwise Wenn nicht hinzugefügt
   * @return Entity für Chaining
   */
  public <R> T insert(T entity, Function<T, R> action, Function<T, R> otherwise, List<Object> parameters) {
    if (action == null) action = t -> null;
    return executeUpdate(insertString(), true, entity, action, otherwise, parameters);
  }

  public <R> T insert(T entity, Function<T, R> action, Function<T, R> otherwise) {
    if (action == null) action = t -> null;
    return executeUpdate(insertString(), true, entity, action, otherwise, List.of());
  }

  public void update(List<Object> parameters) {
    executeUpdate((query == null || query.isBlank()) ? updateString() : query, false, null, t -> null, t -> null, parameters);
  }

  public void update(int id) {
    forId(id).update(List.of());
  }

  public void update(int id, List<Object> parameters) {
    forId(id).update(parameters);
  }

  public void delete(int id) {
    forId(id).delete(List.of());
  }

  public void delete(int id, List<Object> parameters) {
    forId(id).delete(parameters);
  }

  public void delete(List<Object> parameters) {
    executeUpdate(deleteString(), false, null, t -> null, t -> null, parameters);
  }

  private <R> T executeUpdate(String query, boolean insert, T entity, Function<T, R> action, Function<T, R> otherWise, List<Object> parameters) {
    return executeUpdate(query, insert, entity, action, otherWise, parameters, false);
  }

  private <R> T executeUpdate(String query, boolean insert, T entity, Function<T, R> action, Function<T, R> otherWise, List<Object> parameters, boolean retry) {
    final Connection connection = Database.connection().getConnection();
    if (Const.SHOW_SQL) new Console(query).debug();
    try (final PreparedStatement statement = insert ? connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS) : connection.prepareStatement(query)) {
      setValues(statement, parameters, query, false);
      statement.executeUpdate();
      if (entity == null) return null;

      try (final ResultSet generatedKeys = statement.getGeneratedKeys()) {
        if (generatedKeys.next()) { // id != 0
          final int id = generatedKeys.getInt(1);
          if (entity.getId() == 0) {
            entity.setId(id);
            action.apply(entity);
          }
        } else {
          if (entity.getId() == 0) {
            final String where = fields.stream().filter(f -> f instanceof SQLField.Key).map(f -> "`" + f.getColumnName() + "` = ?").collect(Collectors.joining(" AND "));
            final List<Object> objects = fields.stream().filter(f -> f instanceof SQLField.Key).map(SQLField::getValue).toList();
            final List<Object[]> single = new Query<>("SELECT * FROM " + getTableName() + " WHERE " + where, objects).list(objects);
            if (!single.isEmpty()) {
              final int i = SQLUtils.intValue(single.get(0)[0]);
              if (i == 0) throw new SQLException("ID NOT FOUND!!!");
              entity.setId(i);
            }
          }
          otherWise.apply(entity);
        }
      }
      return entity;
    } catch (SQLNonTransientConnectionException exception) {
      if (retry) throw new RuntimeException(exception);
      Database.disconnect();
      return executeUpdate(query, insert, entity, action, otherWise, parameters, true);
    } catch (MysqlDataTruncation exception) {
      String message = "Query nicht zulässig: " + query + "\nParameters: " +
                       parameters.stream().map(Object::toString)
                                 .collect(Collectors.joining(", "));
      if (!Const.SHOW_SQL) new Console(message).severe(exception);
      new DevInfo(message).with(Console.class).severe(exception);
      throw new IllegalArgumentException(exception);
    } catch (SQLException exception) {
      if (!Const.SHOW_SQL) new Console("Query nicht zulässig: " + query).severe(exception);
      new DevInfo("Query nicht zulässig: " + query).with(Console.class).severe(exception);
      throw new IllegalArgumentException(exception);
    }
  }

  public Object[] single() {
    final List<Object[]> results = list(1);
    return results.isEmpty() ? null : results.get(0);
  }

  public Object[] single(List<Object> parameters) {
    final List<Object[]> results = limit(1).list(parameters);
    return results.isEmpty() ? null : results.get(0);
  }

  public List<Object[]> list(int limit) {
    return limit(limit).list();
  }

  public List<Object[]> list() {
    return limit(limit).list(List.of());
  }

  public List<Object[]> list(List<Object> parameters) {
    return list(parameters, false);
  }

  public List<Object[]> list(List<Object> parameters, boolean force) {
    return list(parameters, force, false);
  }

  private List<Object[]> list(List<Object> parameters, boolean force, boolean retry) {
    List<? extends Class<?>> clazzes = new ArrayList<>(fields.stream().filter(sqlField -> sqlField instanceof SQLReturnField).map(o -> (SQLReturnField) o)
        .map(SQLReturnField::getReturnType).toList());

    final String selectQuery = (query == null || query.isBlank()) ? getSelectString(true) : query;
    if (Const.SHOW_SQL) new Console(selectQuery).debug();
    try (final PreparedStatement statement = Database.connection().getConnection().prepareStatement(selectQuery)) {
      setValues(statement, parameters, selectQuery, force);

      final List<Object[]> out = new ArrayList<>();
      try (final ResultSet resultSet = statement.executeQuery()) {
        clazzes = adjust(clazzes, statement);

        while (resultSet.next()) {
          final Object[] data = getRow(resultSet, clazzes);
          out.add(data);
          if (out.size() > limit) break;
        }
      }
      return out;
    } catch (SQLNonTransientConnectionException exception) {
      if (retry) throw new RuntimeException(exception);
      Database.disconnect();
      return list(parameters, force, true);
    } catch (SQLException exception) {
      if (!Const.SHOW_SQL) new Console("Query nicht zulässig: " + selectQuery).severe(exception);
      new DevInfo("Query nicht zulässig: " + selectQuery).with(Console.class).severe(exception);
      throw new IllegalArgumentException(exception);
    }
  }

  public T entity(Object id) {
    if (id == null) return null;
    return entity((int) id);
  }

  public T entity(int id) {
    forId(id);
    final T stored = findEntityStoredById(id);
    if (stored != null) {
      savedCount++;
      if (savedCount % 50_000 == 0) System.out.println("saved " + Math.round(savedCount * 100.0 / (savedCount + queryCount)) + "% of " + Math.round(queryCount / 1000.) + "k - " + concurrentCount + "x errors - stored " + entities.size() + ")");
      return stored;
    }

    final T entity = entity();
    if (entity == null) return null;

    queryCount++;
    entities.put(entity, LocalDateTime.now());
    return entity;
  }

  private T findEntityStoredById(int id) {
    Id e;
    try {
      e = entities.keySet().stream().filter(Objects::nonNull).filter(entity -> entity.getId() == id).filter(entity -> entity.getClass().isAssignableFrom(targetId)).findFirst().orElse(null);
    } catch (ConcurrentModificationException ignored) {
      concurrentCount++;
      e = forId(id).entity();
    }

    if (e == null) return null;

    entities.replace(e, LocalDateTime.now());
    return (T) e;
  }

  public T entity() {
    return entity(List.of());
  }

  public T entity(List<Object> objects) {
    final List<T> results = limit(1).entityList(objects);
    return results.isEmpty() ? null : results.stream().findFirst().orElse(null);
  }

  public List<T> entityList(int limit) {
    return limit(limit).entityList();
  }

  public List<T> entityList() {
    return determineEntityList(list(List.of()));
  }

  public List<T> entityList(List<Object> parameters) {
    return determineEntityList(list(parameters));
  }

  @SuppressWarnings("unchecked")
  private List<T> determineEntityList(List<Object[]> objectList) {
    if (targetId == null) throw new NullPointerException("Entity kann nicht generiert werden");

    final List<T> out = SortedList.of();
    try {
      if (Entity.class.isAssignableFrom(targetId)) {
        final Method getMethod = targetId.getMethod("get", List.class);
        for (final Object[] objects : objectList) {
          final T object = (T) getMethod.invoke(null, Arrays.stream(objects).toList());
          out.add(object);
        }

      } else {
        final Map<String, Class<?>> classes = determineSubClasses();
        for (Object[] objects : objectList) {
          final Class<?> aClass = classes.get((String) objects[1]);
          if (aClass == null) {
            new DevInfo("Error bei der Deklarierung: " + objects[1] + " nicht in " + String.join(", ", classes.keySet())).with(Console.class).severe();
            throw new NoSuchMethodException(objects[1] + ".get(List<Object>)");
          }
          final Method getMethod = aClass.getMethod("get", List.class);
          final T object = (T) getMethod.invoke(null, Arrays.stream(objects).toList());
          out.add(object);
        }
      }
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      new DevInfo("Funktion nicht verfügbar").error(e);
      throw new RuntimeException(e);
    }

    return out;
  }

  @NotNull
  private Map<String, Class<?>> determineSubClasses() {
    final Map<String, Class<?>> classes = new HashMap<>();
    for (final Class<?> aClass : new Reflections("de.xahrie.trues").get(Scanners.SubTypes.of(targetId).asClass())) {
      final Table annotation = aClass.getAnnotation(Table.class);
      if (annotation != null) classes.put(annotation.department(), aClass);
    }
    return classes;
  }

  @SuppressWarnings("unchecked")
  public Integer id() {
    fields.clear();
    fields.add(SQLReturnField.idOf((Class<Id>) targetId));
    final Object[] entity = single();
    return entity == null ? null : (Integer) entity[0];
  }

  public <C extends Id> C convert(Class<C> targetClass) {
    final List<C> results = convertList(targetClass, 1);
    return results.isEmpty() ? null : results.stream().findFirst().orElse(null);
  }

  public List<T> convertList(List<Object> parameters) {
    fields.clear();
    getAll(targetId);
    final List<Object[]> objectsList = list(parameters);
    if (objectsList.isEmpty()) return List.of();
    if (objectsList.get(0).length == 1) return objectsList.stream().map(objs -> new Query<>(targetId).entity(objs[0])).collect(Collectors.toCollection(SortedList::of));
    else return new Query<>(targetId).get("_" + getTableName() + ".*", Object.class).determineEntityList(objectsList);
  }

  public <C extends Id> List<C> convertList(Class<C> targetClass) {
    fields.clear();
    getAll(targetClass);
    final List<Object[]> objectsList = list();
    if (objectsList.isEmpty()) return List.of();
    if (objectsList.get(0).length == 1) return objectsList.stream().map(objs -> new Query<>(targetClass).entity(objs[0])).collect(Collectors.toCollection(SortedList::of));
    else return new Query<>(targetClass).get("_" + getTableName() + ".*", Object.class).determineEntityList(objectsList);
  }

  public <C extends Id> List<C> convertList(Class<C> targetClass, int limit) {
    return limit(limit).convertList(targetClass);
  }

  public List<T> entityListOr(Query<T> query) {
    return entityListOr(query, limit);
  }

  public List<T> entityListOr(Query<T> query, int limit) {
    List<T> ts = entityList(limit);
    if (ts.isEmpty()) ts = query.entityList(limit);
    return ts;
  }

  public T entityOr(Query<T> query) {
    T entity = entity();
    if (entity == null) entity = query.entity();
    return entity;
  }

  public <C extends Id> List<C> convertListOr(Class<C> targetClass, Query<C> query) {
    return convertListOr(targetClass, query, limit);
  }

  public <C extends Id> List<C> convertListOr(Class<C> targetClass, Query<C> query, int limit) {
    List<C> entity = convertList(targetClass, limit);
    if (entity.isEmpty()) entity = query.convertList(targetClass, limit);
    return entity;
  }

  public <C extends Id> C convertOr(Class<C> targetClass, Query<T> query) {
    C entity = convert(targetClass);
    if (entity == null) entity = query.convert(targetClass);
    return entity;
  }

  @NotNull
  private static Object[] getRow(ResultSet resultSet, List<? extends Class<?>> clazzes) throws SQLException {
    final Object[] data = new Object[clazzes.size()];
    for (int i = 0; i < clazzes.size(); i++) {
      final Class<?> clazz = clazzes.get(i);
      if (!clazz.equals(Object.class) && Enum.class.isAssignableFrom(clazz)) {
        final Listing listing = clazz.getAnnotation(Listing.class);
        if (listing == null) {
          final RuntimeException exception = new IllegalArgumentException("Dieses Enum ist nicht zulässig.");
          new DevInfo(clazz.getName()).severe(exception);
          throw exception;
        }

        final Object index = resultSet.getObject(i + 1);
        data[i] = index == null ? null : new SQLEnum<>(clazz.asSubclass(Enum.class)).of(switch (listing.value()) {
          case ORDINAL -> (int) index - listing.start();
          case LOWER, CUSTOM, UPPER, CAPITALIZE -> index;
        });
        continue;
      }

      final Object field = resultSet.getObject(i + 1);
      if (field == null) data[i] = null;
      else if (field instanceof LocalDateTime dateTime) data[i] = dateTime;
      else if (field instanceof Timestamp timestamp) data[i] = timestamp.toLocalDateTime();
      else if (field instanceof Date date) data[i] = date.toLocalDate();
      else if (field instanceof Time time) data[i] = time.toLocalTime();
      else if (field instanceof String) data[i] = field;
      else if (field instanceof Number) data[i] = field;
      else if (field instanceof Boolean) data[i] = field;
      else {
        final RuntimeException ex = new UnknownFormatConversionException("Das Format ist nicht bekannt.");
        new DevInfo(field + " is " + field.getClass().getSimpleName()).severe(ex);
        throw ex;
      }
    }
    return data;
  }

  private List<? extends Class<?>> adjust(List<? extends Class<?>> clazzes, PreparedStatement statement) throws SQLException {
    final int columns = statement.getMetaData().getColumnCount();
    return new ArrayList<>(IntStream.range(0, columns)
        .mapToObj(i -> (clazzes.isEmpty() ? Object.class : clazzes.get(i % clazzes.size())))
        .toList());
  }
}
