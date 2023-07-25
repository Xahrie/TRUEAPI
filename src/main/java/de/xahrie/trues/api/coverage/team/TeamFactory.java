package de.xahrie.trues.api.coverage.team;

import de.xahrie.trues.api.coverage.team.model.PRMTeam;
import de.xahrie.trues.api.database.query.Query;
import org.jetbrains.annotations.Nullable;

public class TeamFactory {
  @Nullable
  public static PRMTeam getTeam(int teamId) {
    final PRMTeam team = new Query<>(PRMTeam.class).where("prm_id", teamId).entity();
    if (team != null) return team;

    final TeamLoader teamLoader = TeamLoader.create(teamId);
    return teamLoader == null ? null : teamLoader.getTeam();
  }

  @Nullable
  public static TeamLoader getTeamLoader(int teamId) {
    final PRMTeam team = new Query<>(PRMTeam.class).where("prm_id", teamId).entity();
    return team != null ? new TeamLoader(team) : TeamLoader.create(teamId);
  }

  public static PRMTeam getTeam(int prmId, String name, String abbreviation) {
    PRMTeam team = new Query<>(PRMTeam.class).where("prm_id", prmId).entity();
    if (team != null) return team;

    team = fromName(name, abbreviation);
    if (team != null) {
      team.setPrmId(prmId);
      return team;
    }

    return new PRMTeam(prmId, name, abbreviation).create();
  }

  @Nullable
  public static PRMTeam fromName(String name, String abbreviation) {
    return new Query<>(PRMTeam.class).where("team_name", name).and("team_abbr", abbreviation).entity();
  }

  @Nullable
  public static PRMTeam fromAbbreviation(String abbreviation) {
    return new Query<>(PRMTeam.class).where("team_abbr", abbreviation).entity();
  }

  @Nullable
  public static PRMTeam fromName(String name) {
    return new Query<>(PRMTeam.class).where("team_name", name).entity();
  }
}
