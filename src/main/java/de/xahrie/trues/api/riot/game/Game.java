package de.xahrie.trues.api.riot.game;

import de.xahrie.trues.api.coverage.match.model.Match;
import de.xahrie.trues.api.database.connector.SQLUtils;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.database.query.SQLEnum;
import de.xahrie.trues.api.riot.performance.TeamPerf;
import de.xahrie.trues.api.util.Util;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Getter
@Table("game")
@ExtensionMethod(SQLUtils.class)
public class Game implements Entity<Game>, Comparable<Game> {
  @Serial
  private static final long serialVersionUID = -1636645398510925983L;

  @Setter
  private int id; // game_id
  private final String gameId; // game_index
  private final LocalDateTime start; // start_time
  private final int durationInSeconds; // duration
  private final GameType type; // game_type
  private boolean orgaGame = false; // orgagame
  private Integer matchId; // coverage
  private Match match; // coverage

  public Match getMatch() {
    if (match == null) this.match = new Query<>(Match.class).entity(matchId);
    return match;
  }

  private List<Selection> selections;

  public List<Selection> getSelections() {
    if (selections == null) this.selections = new Query<>(Selection.class).where("game", this).entityList();
    return selections;
  }

  public boolean addSelection(Selection selection) {
    return getSelections().add(selection);
  }
  private List<TeamPerf> teamPerformances;

  public List<TeamPerf> getTeamPerformances() {
    if (teamPerformances == null) this.teamPerformances = new Query<>(TeamPerf.class).where("game", this).entityList();
    return teamPerformances;
  }

  public boolean addTeamPerformance(TeamPerf teamPerf) {
    return getTeamPerformances().add(teamPerf);
  }

  private Game(int id, String gameId, LocalDateTime start, int durationInSeconds, GameType type, boolean orgaGame, Integer matchId) {
    this.id = id;
    this.gameId = gameId;
    this.start = start;
    this.durationInSeconds = durationInSeconds;
    this.type = type;
    this.orgaGame = orgaGame;
    this.matchId = matchId;
  }

  public static Game get(List<Object> objects) {
    return new Game(
        objects.get(0).intValue(),
        (String) objects.get(1),
        (LocalDateTime) objects.get(2),
        (int) objects.get(3),
        new SQLEnum<>(GameType.class).of(objects.get(4)),
        (boolean) objects.get(5),
        (Integer) objects.get(6)
    );
  }

  @Override
  public Game create() {
    return new Query<>(Game.class).key("game_index", gameId)
        .col("start_time", start).col("duration", durationInSeconds).col("game_type", type).col("orgagame", orgaGame)
        .col("coverage", matchId)
        .insert(this);
  }

  public void setOrgaGame(boolean orgaGame) {
    if (orgaGame != this.orgaGame) new Query<>(Game.class).col("orgagame", orgaGame).update(id);
    this.orgaGame = orgaGame;
  }

  public void setMatch(@NonNull Match match) {
    if (Objects.equals(getMatch(), match)) return;
    this.matchId = Util.avoidNull(match, Match::getId);
    this.match = match;
    new Query<>(Game.class).col("coverage", matchId).update(id);
  }

  public boolean hasSelections() {
    return !getSelections().isEmpty();
  }

  public String getDuration() {
    return Util.formatDuration(durationInSeconds);
  }

  public LocalDateTime getEnd() {
    return start.plusSeconds(durationInSeconds);
  }

  @Override
  public int compareTo(@NotNull Game o) {
    return Comparator.comparing(Game::getStart).reversed().compare(this, o);
  }
}
