package de.xahrie.trues.api.coverage.team.model;

import java.io.Serial;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Objects;

import de.xahrie.trues.api.coverage.league.model.AbstractLeague;
import de.xahrie.trues.api.coverage.league.model.PRMLeague;
import de.xahrie.trues.api.coverage.stage.model.Stage;
import de.xahrie.trues.api.coverage.team.leagueteam.LeagueTeam;
import de.xahrie.trues.api.coverage.season.PRMSeason;
import de.xahrie.trues.api.coverage.season.SeasonFactory;
import de.xahrie.trues.api.database.connector.SQLUtils;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.JoinQuery;
import de.xahrie.trues.api.database.query.ModifyOutcome;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@Table(value = "team", department = "prime")
@ExtensionMethod({SQLUtils.class, StringUtils.class})
public class PRMTeam extends AbstractTeam implements Entity<PRMTeam> {
  @Serial private static final long serialVersionUID = -8914567031452407982L;

  private Integer prmId; // prm_id
  private TeamRecord record;

  public PRMTeam(int prmId, String name, String abbreviation) {
    super(name, abbreviation);
    this.setPrmId(prmId);
  }

  public PRMTeam(int id, String name, String abbreviation, LocalDateTime refresh, boolean highlight, Integer lastMMR, Integer prmId, TeamRecord record) {
    super(id, name, abbreviation, refresh, highlight, lastMMR);
    this.prmId = prmId;
    this.record = record;
  }

  @NotNull
  @Contract("_ -> new")
  public static PRMTeam get(@NotNull List<Object> objects) {
    return new PRMTeam(
        (int) objects.get(0),
        (String) objects.get(2),
        (String) objects.get(3),
        (LocalDateTime) objects.get(4),
        (boolean) objects.get(5),
        (Integer) objects.get(6),
        (Integer) objects.get(7),
        objects.get(8) == null ? null : new TeamRecord(objects.get(8).shortValue(), objects.get(9).shortValue(), objects.get(10).shortValue())
    );
  }

  @Override
  public PRMTeam create() {
    if (refresh == null) this.refresh = LocalDateTime.of(1, Month.JANUARY, 1, 0, 0);
    Query<PRMTeam> q = new Query<>(PRMTeam.class).key("prm_id", prmId)
        .col("team_name", name).col("team_abbr", abbreviation).col("refresh", refresh).col("highlight", highlight).col("last_team_mmr", lastMMR);
    if (record != null) q = q.col("total_wins", record.wins()).col("total_losses", record.losses()).col("seasons", record.seasons());
    final PRMTeam team = q.insert(this);
    if (getOrgaTeam() != null) getOrgaTeam().setTeam(team);
    return team;
  }

  public LeagueTeam getCurrentLeague() {
    final PRMSeason lastPRMSeason = SeasonFactory.getLastPRMSeason();
    if (lastPRMSeason == null) return null;
    return new Query<>(LeagueTeam.class)
        .join(new JoinQuery<>(LeagueTeam.class, AbstractLeague.class))
        .join(new JoinQuery<>(AbstractLeague.class, Stage.class).col("stage"))
        .where("team", this).and("_stage.season", lastPRMSeason)
        .descending("_stage.stage_start").entity();
  }

  @Nullable
  public PRMLeague getLastLeague() {
    return new Query<>(PRMLeague.class, "SELECT _league.* FROM league_team as _leagueteam " +
        "INNER JOIN coverage_group as _league ON _leagueteam.league = _league.coverage_group_id " +
        "INNER JOIN coverage_stage as _stage ON _league.stage = _stage.coverage_stage_id " +
        "WHERE (team = ? and _stage.department <> ? and _league.department = ?) ORDER BY _stage.stage_start DESC LIMIT 1")
        .entity(List.of(this, "Playoffs", "prime"));
  }

  public ModifyOutcome setScore(AbstractLeague division, String score) {
    TeamScore teamScore = TeamScore.of(score);
    final LeagueTeam currentLeague = getCurrentLeague();
    if (teamScore == null) teamScore = currentLeague.getScore();
    final ModifyOutcome outcome;
    if (currentLeague == null || !currentLeague.getLeague().equals(division)) outcome = ModifyOutcome.ADDED;
    else outcome = currentLeague.getScore().equals(teamScore) ? ModifyOutcome.NOTHING : ModifyOutcome.CHANGED;

    new LeagueTeam(division, this, teamScore).create();
    return outcome;
  }

  public void setRecord(@NotNull String record, short seasons) {
    final String wins = record.split(" / ")[0];
    final short winsInteger = Short.parseShort(wins);
    final String losses = record.split(" / ")[1];
    final short lossesInteger = Short.parseShort(losses);
    this.record = new TeamRecord(seasons, winsInteger, lossesInteger);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof final PRMTeam prmTeam)) return false;
    if (prmId == null) return super.equals(o);
    return prmId.equals(prmTeam.getPrmId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getPrmId());
  }
}