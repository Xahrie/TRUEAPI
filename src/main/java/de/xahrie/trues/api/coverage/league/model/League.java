package de.xahrie.trues.api.coverage.league.model;

import java.io.Serial;
import java.util.List;

import de.xahrie.trues.api.coverage.stage.model.Stage;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(value = "coverage_group", department = "other")
public class League extends AbstractLeague implements Entity<League> {
  @Serial
  private static final long serialVersionUID = -1878025702559463286L;

  public League(Stage stage, String name) {
    super(stage, name);
  }

  private League(int id, int stageId, String name) {
    super(id, stageId, name);
  }

  public static League get(List<Object> objects) {
    return new League(
        (int) objects.get(0),
        (int) objects.get(2),
        (String) objects.get(3)
    );
  }

  @Override
  public League create() {
    return new Query<>(League.class).key("stage", stageId).key("group_name", name)
        .insert(this);
  }
}
