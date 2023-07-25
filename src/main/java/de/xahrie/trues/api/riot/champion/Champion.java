package de.xahrie.trues.api.riot.champion;

import java.io.Serial;
import java.util.List;

import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
@Table("champion")
public class Champion implements Entity<Champion> {
  @Serial
  private static final long serialVersionUID = 6171714758229050668L;

  private int id; // champion_id
  private String name; // champion_name

  public static Champion get(List<Object> objects) {
    return new Champion(
        (int) objects.get(0),
        (String) objects.get(1)
    );
  }

  @Override
  public Champion create() {
    return new Query<>(Champion.class)
        .key("champion_id", id)
        .col("champion_name", name)
        .insert(this);
  }

  @Override
  public void setId(int id) {
    this.id = id;
  }
}