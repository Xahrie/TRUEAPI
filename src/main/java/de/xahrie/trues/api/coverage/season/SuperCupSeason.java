package de.xahrie.trues.api.coverage.season;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;

@Table(value = "coverage_season", department = "super_cup")
public class SuperCupSeason extends Season implements Entity<SuperCupSeason> {
  @Serial
  private static final long serialVersionUID = -7533516714481417687L;

  public SuperCupSeason(int id, String name, String fullName, TimeRange range, boolean active) {
    super(id, name, fullName, range, active);
  }

  public static SuperCupSeason get(List<Object> objects) {
    return new SuperCupSeason(
        (int) objects.get(0),
        (String) objects.get(2),
        (String) objects.get(3),
        new TimeRange((LocalDateTime) objects.get(4), (LocalDateTime) objects.get(5)),
        (boolean) objects.get(6)
    );
  }

  @Override
  public SuperCupSeason create() {
    return new Query<>(SuperCupSeason.class)
        .key("season_name", name).key("season_full", fullName)
        .col("season_start", range.getStartTime()).col("season_end", range.getEndTime()).col("active", active).insert(this);
  }
}
