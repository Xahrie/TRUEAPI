package de.xahrie.trues.api.database.query;

import java.util.List;

import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.util.StringUtils;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(StringUtils.class)
public class JoinQuery<E extends Id, J extends Id> {
  private Class<E> targetClass;
  private Class<J> joinedClass;
  private JoinType joinType;
  private String column, column2, alias;
  private Query<E> innerQuery;

  protected Class<E> getTargetClass() {
    return targetClass;
  }

  public Query<E> getInnerQuery() {
    return innerQuery;
  }

  public JoinQuery(Query<E> innerQuery) {
    this.innerQuery = innerQuery;
  }

  public JoinQuery(Class<E> targetClass, Class<J> joinedClass) {
    this(targetClass, joinedClass, JoinType.INNER);
  }

  public JoinQuery(Class<E> targetClass, Class<J> joinedClass, JoinType joinType) {
    this.joinType = joinType;
    this.targetClass = targetClass;
    this.joinedClass = joinedClass;
    this.column = joinedClass.getSimpleName().toLowerCase().replace("abstract", "");
    this.column2 = null;
    this.alias = joinedClass.getSimpleName().toLowerCase().replace("abstract", "");
  }

  public JoinQuery<E, J> as(String alias) {
    this.alias = alias;
    return this;
  }

  public JoinQuery<E, J> col(String column) {
    this.column = column;
    return this;
  }

  public JoinQuery<E, J> ref(String reference) {
    this.column2 = reference;
    return this;
  }

  @Override
  public String toString() {
    if (innerQuery != null) return innerQuery.query;
    final String joinedTableName = joinedClass.getAnnotation(Table.class).value();
    String joinedTableAlias = "_" + (this.alias.startsWith("_") ? this.alias.substring(1) : this.alias);
    if (column.contains(".")) joinedTableAlias = "_" + column.after(".");
    final String col = column.contains(".") ? column : "`_" + targetClass.getSimpleName().toLowerCase().replace("abstract", "") + "`.`" + column + "`";
    final String col2 = joinedTableAlias + "`.`" + (column2 != null ? column2 : joinedTableName + "_id") + "`";
    return joinType.toString() + joinedTableName + "` as `" + joinedTableAlias + "` ON " + col + " = `" + col2;
  }

  public List<Object> getParams() {
    return innerQuery == null ? List.of() : innerQuery.additionalParameters;
  }

  public enum JoinType {
    INNER,
    OUTER,
    LEFT,
    RIGHT;

    @Override
    public String toString() {
      return " " + name() + " JOIN `";
    }
  }
}
