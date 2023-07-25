package de.xahrie.trues.api.database.query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.xahrie.trues.api.database.connector.Table;
import lombok.NonNull;

public class ConditionManager {
  private final List<Condition> conditions = new ArrayList<>();
  private Condition always = null;

  public ConditionManager(Class<?> entityClass) {
    if (entityClass != null) {
      final String department = entityClass.getAnnotation(Table.class).department();
      if (!department.isBlank()) this.always = Condition.compare(Condition.Comparer.EQUAL, "department", department);
    }
  }

  protected void and(@NonNull Condition condition) {
    if (conditions.isEmpty()) {
      or(condition);
      return;
    }
    final Condition lastCondition = conditions.get(conditions.size() - 1);
    conditions.set(conditions.size() - 1, lastCondition.merge(condition, true));
  }

  protected void or(@NonNull Condition condition) {
    conditions.add(condition);
  }

  protected void keep(@NonNull Condition condition) {
    if (always == null) this.always = condition;
    else conditions.set(conditions.size() - 1, always.merge(condition, true));
  }

  protected List<Object> getValues() {
    final List<Object> paramsToAdd = always == null ? new ArrayList<>() : new ArrayList<>(always.getParamsToAdd());
    paramsToAdd.addAll(conditions.stream().flatMap(condition -> condition.getParamsToAdd().stream()).toList());
    return paramsToAdd;
  }

  @Override
  public String toString() {
    final String statement = "(" + conditions.stream().map(Condition::toString).collect(Collectors.joining(") or (")) + ")";
    final String output = always != null ? always + " and (" + statement + ")" : statement;
    return output.replace("(", "").replace(")", "").isBlank() ? "" : "WHERE " + output;
  }
}
