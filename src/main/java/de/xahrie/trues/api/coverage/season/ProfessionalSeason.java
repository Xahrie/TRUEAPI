package de.xahrie.trues.api.coverage.season;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;

@Table(value = "coverage_season", department = "pro")
public class ProfessionalSeason extends Season implements Entity<ProfessionalSeason> {
  @Serial
  private static final long serialVersionUID = 8782660232234358661L;

  public ProfessionalSeason(int id, String name, String fullName, TimeRange range, boolean active) {
    super(id, name, fullName, range, active);
  }

  public static ProfessionalSeason get(List<Object> objects) {
    return new ProfessionalSeason(
        (int) objects.get(0),
        (String) objects.get(2),
        (String) objects.get(3),
        new TimeRange((LocalDateTime) objects.get(4), (LocalDateTime) objects.get(5)),
        (boolean) objects.get(6)
    );
  }

  @Override
  public ProfessionalSeason create() {
    return new Query<>(ProfessionalSeason.class)
        .key("season_name", name).key("season_full", fullName)
        .col("season_start", range.getStartTime()).col("season_end", range.getEndTime()).col("active", active).insert(this);
  }
}
