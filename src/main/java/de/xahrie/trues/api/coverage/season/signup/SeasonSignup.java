package de.xahrie.trues.api.coverage.season.signup;

import java.io.Serial;
import java.util.Comparator;
import java.util.List;

import de.xahrie.trues.api.coverage.season.Season;
import de.xahrie.trues.api.coverage.team.model.PRMTeam;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Table("season_signup")
public class SeasonSignup implements Entity<SeasonSignup>, Comparable<SeasonSignup> {
  @Serial private static final long serialVersionUID = 4493211805830610407L;

  @Setter private int id; // season_signup_id
  private final int seasonId, teamId; // season, team
  private final String info; // signup_info

  public SeasonSignup(@NotNull Season season, @NotNull PRMTeam team, @Nullable String info) {
    this.season = season;
    this.seasonId = season.getId();
    this.team = team;
    this.teamId = team.getId();
    this.info = info;
  }

  public SeasonSignup(int id, int seasonId, int teamId, String info) {
    this.id = id;
    this.seasonId = seasonId;
    this.teamId = teamId;
    this.info = info;
  }

  public static SeasonSignup get(List<Object> objects) {
    return new SeasonSignup(
        (int) objects.get(0),
        (int) objects.get(1),
        (int) objects.get(2),
        (String) objects.get(3)
    );
  }

  @Override
  public SeasonSignup create() {
    return new Query<>(SeasonSignup.class).key("season", seasonId).key("team", teamId)
        .col("signup_info", info).insert(this);
  }

  @Override
  public int compareTo(@NotNull SeasonSignup o) {
    return Comparator.comparing(SeasonSignup::getSeason).compare(this, o);
  }

  private Season season;

  public Season getSeason() {
    if (season == null) this.season = new Query<>(Season.class).entity(seasonId);
    return season;
  }

  private PRMTeam team;

  public PRMTeam getTeam() {
    if (team == null) this.team = new Query<>(PRMTeam.class).entity(teamId);
    return team;
  }
}
