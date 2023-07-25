package de.xahrie.trues.api.riot.performance;

import java.io.Serial;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import de.xahrie.trues.api.database.connector.SQLUtils;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.database.query.SQLEnum;
import de.xahrie.trues.api.riot.KDA;
import de.xahrie.trues.api.riot.game.Game;
import de.xahrie.trues.api.riot.match.Side;
import de.xahrie.trues.api.util.Util;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Table("team_perf")
@ExtensionMethod(SQLUtils.class)
public class TeamPerf implements Entity<TeamPerf>, Comparable<TeamPerf> {
  @Serial
  private static final long serialVersionUID = 4138620147627390023L;

  @Setter
  private int id; // team_perf_id
  private final int gameId; // game
  private final Side side; // first
  private final boolean win; // win
  private final KDA kda;
  private final Integer totalGold; // total_gold
  private final Integer totalDamage; // total_damage
  private final Integer totalVision; // total_vision
  private final Integer totalCreeps; // total_creeps
  private final Objectives objectives; // turrets, drakes, inhibs, heralds, barons;
  private Integer teamId; // team

  private Game game;

  public Game getGame() {
    if (game == null) this.game = new Query<>(Game.class).entity(gameId);
    return game;
  }

  private AbstractTeam team;

  public void setTeam(AbstractTeam team) {
    this.team = team;
    this.teamId = Util.avoidNull(team, AbstractTeam::getId);
    new Query<>(TeamPerf.class).col("team", team).update(id);
  }

  @Nullable
  public AbstractTeam getTeam() {
    if (team == null) this.team = new Query<>(AbstractTeam.class).entity(teamId);
    return team;
  }

  private List<Performance> performances;

  public List<Performance> getPerformances() {
    if (performances == null) this.performances = new Query<>(Performance.class).where("t_perf", this).entityList();
    return performances;
  }

  public boolean addPerformance(Performance performance) {
    return getPerformances().add(performance);
  }

  public TeamPerf(Game game, Side side, boolean win, KDA kda, Integer totalGold, Integer totalDamage, Integer totalVision, Integer totalCreeps, Objectives objectives) {
    this.game = game;
    this.gameId = game.getId();
    this.side = side;
    this.win = win;
    this.kda = kda;
    this.totalGold = totalGold;
    this.totalDamage = totalDamage;
    this.totalVision = totalVision;
    this.totalCreeps = totalCreeps;
    this.objectives = objectives;
  }

  private TeamPerf(int id, int gameId, Side side, boolean win, KDA kda, Integer totalGold, Integer totalDamage, Integer totalVision, Integer totalCreeps, Objectives objectives, Integer teamId) {
    this.id = id;
    this.gameId = gameId;
    this.side = side;
    this.win = win;
    this.kda = kda;
    this.totalGold = totalGold;
    this.totalDamage = totalDamage;
    this.totalVision = totalVision;
    this.totalCreeps = totalCreeps;
    this.objectives = objectives;
    this.teamId = teamId;
  }

  public static TeamPerf get(List<Object> objects) {
    return new TeamPerf(
        objects.get(0).intValue(),
        objects.get(1).intValue(),
        new SQLEnum<>(Side.class).of(objects.get(2)),
        (boolean) objects.get(3),
        new KDA(objects.get(4).shortValue(), objects.get(5).shortValue(), objects.get(6).shortValue()),
        (Integer) objects.get(7),
        (Integer) objects.get(8),
        (Integer) objects.get(9),
        (Integer) objects.get(10),
        new Objectives((int) objects.get(11), (int) objects.get(12), (int) objects.get(13), (int) objects.get(14), (int) objects.get(15)),
        objects.get(16).intValue());
  }

  @Override
  public TeamPerf create() {
    return new Query<>(TeamPerf.class).key("game", gameId).key("first", side).key("win", win)
        .col("kills", kda.kills()).col("deaths", kda.deaths()).col("assists", kda.assists()).col("total_gold", totalGold)
        .col("total_damage", totalDamage).col("total_vision", totalVision).col("total_creeps", totalCreeps)
        .col("turrets", objectives.turrets()).col("drakes", objectives.drakes()).col("inhibs", objectives.inhibs())
        .col("heralds", objectives.heralds()).col("barons", objectives.barons()).col("team", teamId)
        .insert(this, getGame()::addTeamPerformance);
  }

  public TeamPerf getOpponent() {
    return getGame().getTeamPerformances().stream()
        .filter(teamPerf -> teamPerf.getId() != id).findFirst().orElse(null);
  }

  public AbstractTeam getOpposingTeam() {
    return Util.avoidNull(getOpponent(), null, TeamPerf::getTeam);
  }

  public String getWinString() {
    return win ? "Gewonnen" : "Verloren";
  }

  @Override
  public int compareTo(@NotNull TeamPerf o) {
    return Comparator.comparing(TeamPerf::getGame).thenComparing(TeamPerf::getSide).compare(this, o);
  }

  public record Objectives(byte turrets, byte drakes, byte inhibs, byte heralds, byte barons) {
    public Objectives(int turrets, int drakes, int inhibs, int heralds, int barons) {
      this((byte) turrets, (byte) drakes, (byte) inhibs, (byte) heralds, (byte) barons);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof final TeamPerf teamPerf)) return false;
    return getGameId() == teamPerf.getGameId() && getSide() == teamPerf.getSide();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getGameId(), getSide());
  }
}
