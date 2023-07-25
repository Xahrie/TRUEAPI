package de.xahrie.trues.api.database.query;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ModifyOutcome {
  /**
   * <code>null</code> -> <code>not null</code>
   */
  ADDED(true, false),
  /**
   * <code>not null</code> -> <code>not null</code>
   */
  CHANGED(false, false),
  /**
   * <code>null</code> -> <code>null</code>
   */
  NOTHING(true, true),
  /**
   * <code>not null</code> -> <code>null</code>
   */
  REMOVED(false, true);

  private final boolean wasNull, isNull;

  public boolean wasNull() {
    return wasNull;
  }

  public boolean isNull() {
    return isNull;
  }
}