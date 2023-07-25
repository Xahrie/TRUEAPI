package de.xahrie.trues.api.database.query;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UnknownFormatConversionException;
import java.util.stream.Collectors;

import de.xahrie.trues.api.database.connector.Listing;
import de.xahrie.trues.api.util.StringUtils;
import de.xahrie.trues.api.util.io.log.DevInfo;

public class SimpleQueryFormer<T extends Id> extends SimpleAbstractQuery<T> {
  protected final String query;

  public SimpleQueryFormer(Class<T> targetId) {
    this(targetId, null);
  }

  public SimpleQueryFormer(Class<T> targetId, String query) {
    super(targetId);
    this.query = query;
  }

  /**
   * INSERT INTO <b>table</b> (<b>parmameternames</b>) VALUES (<b>values</b>) ON DUPLICATE KEY UPDADE <b>non final</b> <br>
   * 1: <b>table</b> - {@link Class<T>} <br>
   * 2: <b>parameternames - values - onduplicate</b> - list of {@link SQLField} <br>
   * ? auf <b>2</b>
   */
  String insertString() {
    if (getDepartment() != null) key("department", getDepartment());
    final String allFields = fields.stream().map(SQLField::getColumnName).collect(Collectors.joining(", "));
    String allValues = ", ?".repeat(fields.size());
    if (!allValues.isBlank()) allValues = allValues.substring(2);
    final String updateable = fields.stream().filter(columnType -> columnType instanceof SQLField.Updateable).map(SQLField::getColumnName)
        .map(fieldName -> fieldName + " = VALUES(" + fieldName + ")").collect(Collectors.joining(", "));
    final String output = "INSERT INTO " + getTableName() + " (" + allFields + ") VALUES (" + allValues + ")";
    return updateable.isBlank() ? output : output + " ON DUPLICATE KEY UPDATE " + updateable;
  }

  /**
   * UPDATE <b>table</b> JOIN <b>joins</b> SET <b>parmameters</b> WHERE <b>conditions</b> GROUP BY <b>group</b> ORDER BY <b>order</b> LIMIT <br>
   * 1: <b>table</b> - {@link Class<T>} <br>
   * 2: <b>joins</b> - list of {@link JoinQuery} <br>
   * 3: <b>parameters - values</b> - list of {@link SQLField.Updateable} <br>
   * 4: <b>conditions</b> - list of {@link Condition} <br>
   * 5: <b>group</b> - {@link SQLOrder} <br>
   * 6: <b>order</b> - {@link SQLOrder} <br>
   * 7: <b>limit</b> - {@link Integer} <br>
   * ? auf <b>3, 4</b>
   */
  String updateString() {
    return "UPDATE " + getFrom() + getJoins() + " SET " + getFields(" = ?") + getConditions() + getGroup() + getOrder() + getOffset() + getLimit();
  }

  /**
   * SELECT <b>parmameternames</b> FROM <b>table</b> JOIN <b>joins</b> WHERE <b>conditions</b> GROUP BY <b>group</b> ORDER BY <b>order</b> LIMIT <br>
   * 1: <b>parameternames</b> - list of {@link SQLField} <br>
   * 2: <b>table</b> - {@link Class<T>} <br>
   * 3: <b>joins</b> - list of {@link JoinQuery} <br>
   * 4: <b>conditions</b> - list of {@link Condition} <br>
   * 5: <b>group</b> - {@link SQLOrder} <br>
   * 6: <b>order</b> - {@link SQLOrder} <br>
   * 7: <b>limit</b> - {@link Integer} <br>
   * ? auf <b>4</b>
   */
  public String getSelectString(boolean includeLimit) {
    if (getDepartment() != null) where("department", getDepartment());
    StringBuilder mainQuery = new StringBuilder(querySelectString(includeLimit));
    if (unified.isEmpty()) return mainQuery.toString();

    mainQuery = new StringBuilder("(" + mainQuery + ")");
    for (Union union : unified) mainQuery.append(union.type().toString()).append(" (").append(union.query().querySelectString(includeLimit)).append(")");
    return mainQuery.toString();
  }

  String querySelectString(boolean includeLimit) {
    return "SELECT " + getFields("") + " FROM " + getFrom() + getJoins() + getConditions() + getGroup() + getOrder() + getOffset() +
        (includeLimit ? getLimit() : "");
  }

  /**
   * DELETE FROM <b>table</b> JOIN <b>joins</b> WHERE <b>conditions</b> GROUP BY <b>group</b> ORDER BY <b>order</b> LIMIT <br>
   * 1: <b>table</b> - {@link Class<T>} <br>
   * 2: <b>joins</b> - list of {@link JoinQuery} <br>
   * 3: <b>conditions</b> - list of {@link Condition} <br>
   * 4: <b>group</b> - {@link SQLOrder} <br>
   * 5: <b>order</b> - {@link SQLOrder} <br>
   * 6: <b>limit</b> - {@link Integer} <br>
   * ? auf <b>3</b>
   */
  String deleteString() {
    return "DELETE FROM " + getFrom() + getJoins() + getConditions() + getGroup() + getOrder() + getOffset() + getLimit();
  }

  protected String getInnerSimpleQuery() {
    return getSelectString(false);
  }

  protected List<Object> getValues(String query) {
    final List<Object> sqlFields = new ArrayList<>(fields.stream().filter(sqlField -> !(sqlField instanceof SQLReturnField)).map(SQLField::getValue).toList());
    joins.stream().map(JoinQuery::getParams).forEach(sqlFields::addAll);
    if (query.contains("WHERE")) sqlFields.addAll(conditionManager.getValues());
    for (final Union union : unified) {
      sqlFields.addAll(union.query().getValues(union.query().getSelectString(true)));
    }
    return sqlFields;
  }

  protected void setValues(PreparedStatement statement, List<Object> parameters, String q, boolean force) throws SQLException {
    List<Object> values = query == null ? getValues(q) : parameters;
    if (force) {
      values = getValues(q);
      int parameterIndex = 0;
      for (int i = 0; i < values.size(); i++) {
        final Object o = values.get(i);
        if (o.equals("?")) {
          values.set(i, parameters.get(parameterIndex));
          parameterIndex++;
        }
      }
    }
    if (values.isEmpty()) return;

    for (int i = 0; i < values.size(); i++) {
      final Object parameter = values.get(i);
      final int pos = i + 1;
      if (parameter == null) {
        statement.setNull(pos, Types.NULL);
        continue;
      }

      if (parameter instanceof Id entity) {
        statement.setInt(pos, entity.getId());
        continue;
      }

      if (parameter instanceof Enum<?> parameterEnum) {
        final Listing listing = parameterEnum.getClass().getAnnotation(Listing.class);
        if (listing == null) {
          final RuntimeException exception = new IllegalArgumentException("Dieses Enum ist nicht zulässig.");
          new DevInfo(parameterEnum + " is " + parameterEnum.getClass().getSimpleName()).severe(exception);
          throw exception;
        }

        switch (listing.value()) {
          case CUSTOM -> statement.setString(pos, parameterEnum.toString());
          case UPPER -> statement.setString(pos, parameterEnum.name().toUpperCase());
          case LOWER -> statement.setString(pos, parameterEnum.name().toLowerCase());
          case CAPITALIZE -> statement.setString(pos, StringUtils.capitalizeEnum(parameterEnum.name().toLowerCase()));
          case ORDINAL -> statement.setByte(pos, (byte) (parameterEnum.ordinal() + listing.start()));
        }

        if (listing.value().equals(Listing.ListingType.CUSTOM)) statement.setString(pos, parameterEnum.toString());
        else if (listing.value().equals(Listing.ListingType.ORDINAL)) {
          final byte index = (byte) (parameterEnum.ordinal() + listing.start());
          statement.setByte(pos, index);
        }
        continue;
      }

      if (parameter instanceof LocalDateTime dateTime) statement.setTimestamp(pos, Timestamp.valueOf(dateTime));
      else if (parameter instanceof LocalDate date) statement.setDate(pos, Date.valueOf(date));
      else if (parameter instanceof String paramString) statement.setString(pos, paramString);
      else if (parameter instanceof Boolean paramBool) statement.setBoolean(pos, paramBool);
      else if (parameter instanceof Byte paramByte) statement.setByte(pos, paramByte);
      else if (parameter instanceof Short paramShort) statement.setShort(pos, paramShort);
      else if (parameter instanceof Integer paramInteger) statement.setInt(pos, paramInteger);
      else if (parameter instanceof Long paramLong) statement.setLong(pos, paramLong);
      else {
        final RuntimeException ex = new UnknownFormatConversionException("Das Format ist nicht bekannt.");
        new DevInfo(parameter + " is " + parameter.getClass().getSimpleName()).severe(ex);
        throw ex;
      }
    }
  }

  private String getFields(String suffix) {
    if (fields.isEmpty() && subEntity == null) return "*";

    String out = fields.stream()
        .map(sqlField -> ((sqlField instanceof SQLReturnField returnField && returnField.isDistinct()) ? "DISTINCT " : "") +
            sqlField.getColumnName() + suffix)
        .collect(Collectors.joining(", "));
    if (subEntity != null) out += "`_" + subEntity.getSimpleName().toLowerCase().replace("abstract", "") + "`.*";
    return out;
  }

  private String getFrom() {
    return getTableName() + " as `_" + targetId.getSimpleName().toLowerCase().replace("abstract", "") + "`";
  }

  private String getJoins() {
    return joins.isEmpty() ? "" : joins.stream().map(JoinQuery::toString).collect(Collectors.joining(""));
  }

  private String getConditions() {
    return " " + conditionManager;
  }

  private String getGroup() {
    return (group == null) ? "" : " GROUP BY " + group;
  }

  private String getOrder() {
    return (order == null) ? "" : " ORDER BY " + order;
  }

  private String getOffset() {
    return offset == null ? "" : " OFFSET " + offset;
  }

  private String getLimit() {
    return " LIMIT " + limit;
  }
}
