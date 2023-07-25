package de.xahrie.trues.api.coverage.season;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;
import lombok.Getter;

@Getter
@Table(value = "coverage_season", department = "prime")
public class PRMSeason extends Season implements Entity<PRMSeason> {
  @Serial
  private static final long serialVersionUID = -3519857892120876511L;

  private final int prmId; // season_id

  public PRMSeason(int id, String name, String fullName, TimeRange range, boolean active, int prmId) {
    super(id, name, fullName, range, active);
    this.prmId = prmId;
  }

  public static PRMSeason get(List<Object> objects) {
    return new PRMSeason(
        (int) objects.get(0),
        (String) objects.get(2),
        (String) objects.get(3),
        new TimeRange((LocalDateTime) objects.get(4), (LocalDateTime) objects.get(5)),
        (boolean) objects.get(6),
        (int) objects.get(7)
    );
  }

  @Override
  public PRMSeason create() {
    return new Query<>(PRMSeason.class)
        .key("season_name", name).key("season_full", fullName).key("season_id", prmId)
        .col("season_start", range.getStartTime()).col("season_end", range.getEndTime()).col("active", active).insert(this);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof final PRMSeason prmSeason)) return false;
    return this == o || getPrmId() == prmSeason.getPrmId();
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getPrmId());
  }
}
