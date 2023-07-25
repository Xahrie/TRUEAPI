package de.xahrie.trues.api.riot.clash;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

import de.xahrie.trues.api.coverage.season.Season;
import de.xahrie.trues.api.database.connector.SQLUtils;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.util.StringUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;

@Getter
@Table("clash")
@ExtensionMethod(SQLUtils.class)
public class Clash implements Entity<Clash>, Comparable<Clash> {
  @Serial
  private static final long serialVersionUID = 3155389465732347561L;

  @Setter
  private int id; // clash_id
  private final short clashId; // clash_index
  private final int seasonId; // season
  private final String name; // clash_name
  private final LocalDateTime registrationStart; // registration
  private final LocalDateTime clashStart; // clash_start
  private final boolean active; // active

  private Season season;

  public Season getSeason() {
    if (season == null) this.season = new Query<>(Season.class).entity(seasonId);
    return season;
  }

  public Clash(short clashId, @NonNull Season season, String name, LocalDateTime registrationStart, LocalDateTime clashStart, boolean canceled) {
    this.clashId = clashId;
    this.seasonId = season.getId();
    this.season = season;
    this.name = StringUtils.capitalizeEnum(name);
    this.registrationStart = registrationStart;
    this.clashStart = clashStart;
    this.active = !canceled;
  }

  private Clash(int id, short clashId, int seasonId, String name, LocalDateTime registrationStart, LocalDateTime clashStart, boolean active) {
    this.id = id;
    this.clashId = clashId;
    this.seasonId = seasonId;
    this.name = name;
    this.registrationStart = registrationStart;
    this.clashStart = clashStart;
    this.active = active;
  }

  public static Clash get(List<Object> objects) {
    return new Clash(
        (int) objects.get(0),
        objects.get(1).shortValue(),
        (int) objects.get(2),
        (String) objects.get(3),
        (LocalDateTime) objects.get(4),
        (LocalDateTime) objects.get(5),
        (boolean) objects.get(6)
    );
  }

  @Override
  public Clash create() {
    return new Query<>(Clash.class).key("clash_index", clashId)
        .col("season", seasonId).col("clash_name", name).col("registration", registrationStart).col("clash_start", clashStart)
        .col("active", active)
        .insert(this);
  }

  @Override
  public int compareTo(@NotNull Clash o) {
    return clashStart.compareTo(o.getClashStart());
  }
}
