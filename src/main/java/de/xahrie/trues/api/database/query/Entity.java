package de.xahrie.trues.api.database.query;

import java.io.Serializable;

import de.xahrie.trues.api.database.connector.Database;

public interface Entity<T extends Entity<T>> extends Serializable, Id {
  T create();

  default T forceCreate() {
    final T entity = create();
    Database.connection().commit();
    return entity;
  }

  default void update() {
    create();
  }

  default void forceUpdate() {
    update();
    Database.connection().commit();
  }

  default void delete() {
    Query.remove(this);
    new Query<>((Class<T>) getClass()).delete(getId());
  }

  default void forceDelete() {
    delete();
    Database.connection().commit();
  }

  default T refresh(Class<T> entityClass) {
    return new Query<>(entityClass).entity(getId());
  }
}
