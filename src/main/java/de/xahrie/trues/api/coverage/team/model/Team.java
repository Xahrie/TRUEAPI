package de.xahrie.trues.api.coverage.team.model;

import java.io.Serial;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Table(value = "team", department = "other")
public class Team extends AbstractTeam implements Entity<Team> {
  @Serial private static final long serialVersionUID = 2619046560165024773L;

  public Team(String name, String abbreviation) {
    super(name, abbreviation);
  }

  public Team(int id, String name, String abbreviation, LocalDateTime refresh, boolean highlight, Integer lastMMR) {
    super(id, name, abbreviation, refresh, highlight, lastMMR);
  }

  public static Team get(List<Object> objects) {
    return new Team(
        (int) objects.get(0),
        (String) objects.get(2),
        (String) objects.get(3),
        (LocalDateTime) objects.get(4),
        (boolean) objects.get(5),
        (Integer) objects.get(6)
    );
  }

  @Override
  public Team create() {
    if (refresh == null) this.refresh = LocalDateTime.of(1, Month.JANUARY, 1, 0, 0);
    final Team team = new Query<>(Team.class)
        .col("team_name", name).col("team_abbr", abbreviation).col("refresh", refresh).col("highlight", highlight)
        .col("last_team_mmr", lastMMR)
        .insert(this);
    if (getOrgaTeam() != null) getOrgaTeam().setTeam(team);
    return team;
  }
}
