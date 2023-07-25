package de.xahrie.trues.api.coverage.season;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;

@Table(value = "coverage_season", department = "intern")
public class OrgaCupSeason extends Season implements Entity<OrgaCupSeason> {
  @Serial
  private static final long serialVersionUID = -911422537513573644L;

  public OrgaCupSeason(int id, String name, String fullName, TimeRange range, boolean active) {
    super(id, name, fullName, range, active);
  }

  public static OrgaCupSeason get(List<Object> objects) {
    return new OrgaCupSeason(
        (int) objects.get(0),
        (String) objects.get(2),
        (String) objects.get(3),
        new TimeRange((LocalDateTime) objects.get(4), (LocalDateTime) objects.get(5)),
        (boolean) objects.get(6)
    );
  }

  @Override
  public OrgaCupSeason create() {
    return new Query<>(OrgaCupSeason.class)
        .key("season_name", name).key("season_full", fullName)
        .col("season_start", range.getStartTime()).col("season_end", range.getEndTime()).col("active", active).insert(this);
  }

  public static final String RULES = """
        Du darfst nur in Teams spielen, die sich nicht begegnen.
        Stand-ins dürfen höchstens 100 LP über dem ersetzten Spieler sein.
        
        Gruppenspiele: Single-Round-Robin 'Two-Games'
        Elimination: 'Best of Three' (Finale Bo5)
        Einigt euch vor dem offiziellen Termin auf einen Termin.
        Nicht ausgespielte Spiele werden 0:0 gewertet.

        Lineup-Deadline: 24 Stunden vorher
        Lobbyname: 'TRUE M<id> TEAM1 vs TEAM2 G<1-5>' Passwort: 'truecup'
        """;

  @Override
  public boolean isBetable() {
    return super.isBetable();
  }
}
