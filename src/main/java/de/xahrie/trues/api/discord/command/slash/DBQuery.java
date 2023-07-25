package de.xahrie.trues.api.discord.command.slash;

import java.util.List;

import de.xahrie.trues.api.discord.builder.queryCustomizer.Enumeration;

public record DBQuery(String title, String description, List<Column> columns, List<String> params, Enumeration enumeration) {
  public DBQuery(String title, String description, List<Column> columns) {
    this(title, description, columns, List.of(), Enumeration.NONE);
  }

  public DBQuery(String title, String description, List<Column> columns, Enumeration enumeration) {
    this(title, description, columns, List.of(), enumeration);
  }

  public DBQuery(String title, String description, List<Column> columns, List<String> params) {
    this(title, description, columns, params, Enumeration.NONE);
  }

  public void setColumnName(int index, String name) {
    final Column column = columns.get(index);
    column.setName(name);
  }
}
