package de.xahrie.trues.api.coverage.league.model;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import de.xahrie.trues.api.coverage.match.model.LeagueMatch;
import de.xahrie.trues.api.coverage.stage.model.Stage;
import de.xahrie.trues.api.coverage.team.leagueteam.LeagueTeam;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Id;
import de.xahrie.trues.api.database.query.Query;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@Table("coverage_group")
public abstract class AbstractLeague implements ALeague, Id {
  private int id; // coverage_id
  protected final int stageId;
  protected final String name; // group_name

  public AbstractLeague(Stage stage, String name) {
    this.stage = stage;
    this.stageId = stage.getId();
    this.name = name;
  }

  protected AbstractLeague(int id, int stageId, String name) {
    this.id = id;
    this.stageId = stageId;
    this.name = name;
  }

  public boolean isOrgaLeague() {
    return getLeagueTeams().stream().anyMatch(leagueTeam -> leagueTeam.getTeam().getOrgaTeam() != null);
  }

  public List<LeagueMatch> getMatches() {
    return new Query<>(LeagueMatch.class).where("coverage_group", this).entityList();
  }

  public List<LeagueTeam> getLeagueTeams() {
    return new Query<>(LeagueTeam.class).where("league", this).entityList();
  }

  public List<LeagueTeam> getSignups() {
    return new Query<>(LeagueTeam.class).where("league", this).entityList().stream().toList();
  }

  @Override
  public int compareTo(@NotNull ALeague o) {
    return Comparator.comparing(ALeague::getStage).compare(this, o);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof final AbstractLeague league)) return false;
    return getId() == league.getId();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }

  private Stage stage; // stage

  public Stage getStage() {
    if (stage == null) this.stage = new Query<>(Stage.class).entity(stageId);
    return stage;
  }
}
