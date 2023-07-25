package de.xahrie.trues.api.database.query;

import lombok.RequiredArgsConstructor;

public record Union(Query<? extends Id> query, UnionType type) {
  @RequiredArgsConstructor
  public enum UnionType {
    UNION(" UNION DISTINCT"),
    INTERSECT(" INTERSECT"),
    EXCEPT(" EXCEPT");
    private final String display;

    @Override
    public String toString() {
      return display;
    }
  }
}
