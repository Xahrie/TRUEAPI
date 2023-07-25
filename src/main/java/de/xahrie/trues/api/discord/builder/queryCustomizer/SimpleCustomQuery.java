package de.xahrie.trues.api.discord.builder.queryCustomizer;


import java.util.List;

import de.xahrie.trues.api.discord.command.slash.Column;
import de.xahrie.trues.api.discord.command.slash.DBQuery;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public final class SimpleCustomQuery {
  private final NamedQuery namedQuery;
  private final List<Object> parameters;
  @Deprecated
  private List<Object[]> customData;
  private final int frequencyInMinutes;

  public static SimpleCustomQuery of(NamedQuery namedQuery) {
    return new SimpleCustomQuery(namedQuery, List.of(), List.of(), 60);
  }

  public static SimpleCustomQuery of(NamedQuery namedQuery, int frequencyInMinutes) {
    return new SimpleCustomQuery(namedQuery, List.of(), List.of(), frequencyInMinutes);
  }

  public static SimpleCustomQuery params(NamedQuery namedQuery, List<Object> parameters) {
    return new SimpleCustomQuery(namedQuery, parameters, List.of(), 60);
  }

  public static SimpleCustomQuery params(NamedQuery namedQuery, List<Object> parameters, int frequencyInMinutes) {
    return new SimpleCustomQuery(namedQuery, parameters, List.of(), frequencyInMinutes);
  }

  public static SimpleCustomQuery custom(NamedQuery namedQuery, List<Object[]> customData) {
    return new SimpleCustomQuery(namedQuery, List.of(), customData, 60);
  }

  public static SimpleCustomQuery custom(NamedQuery namedQuery, List<Object[]> customData, int frequencyInMinutes) {
    return new SimpleCustomQuery(namedQuery, List.of(), customData, frequencyInMinutes);
  }

  private SimpleCustomQuery(NamedQuery namedQuery, List<Object> parameters, List<Object[]> customData, int frequencyInMinutes) {
    this.namedQuery = namedQuery;
    this.parameters = parameters;
    this.customData = customData;
    this.frequencyInMinutes = frequencyInMinutes;
  }

  public SimpleCustomQuery params(List<Object> parameters) {
    this.parameters.addAll(parameters);
    return this;
  }

  @Deprecated
  public SimpleCustomQuery custom(List<Object[]> customData) {
    this.customData = customData;
    return this;
  }

  @NonNull
  public List<Object[]> build() {
    return customData.isEmpty() ? namedQuery.getQuery().list(parameters.stream().toList(), true) : customData;
  }

  @NonNull
  public List<Column> getColumns() {
    return namedQuery.getDbQuery().columns();
  }

  @NonNull
  public DBQuery getQuery() {
    return namedQuery.getDbQuery();
  }

  public Enumeration getEnumeration() {
    return namedQuery.getDbQuery().enumeration();
  }

  @Nullable
  public String getHeadTitle() {
    return namedQuery.getDbQuery().title();
  }

  @Nullable
  public String getHeadDescription() {
    return namedQuery.getDbQuery().description();
  }

  @NonNull
  public List<Object> getParameters() {
    return parameters;
  }

  @NonNull
  public String getName() {
    return namedQuery.name();
  }

  @Nullable
  public List<List<Object[]>> getData() {
    return namedQuery.getCustom();
  }

  public NamedQuery getNamedQuery() {
    return namedQuery;
  }

  public int getFrequencyInMinutes() {
    return frequencyInMinutes;
  }
}
