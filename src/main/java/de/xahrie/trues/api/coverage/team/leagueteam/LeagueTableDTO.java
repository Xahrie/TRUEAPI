package de.xahrie.trues.api.coverage.team.leagueteam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import de.xahrie.trues.api.coverage.match.model.Match;
import de.xahrie.trues.api.coverage.participator.model.Participator;
import de.xahrie.trues.api.coverage.team.model.Standing;
import de.xahrie.trues.api.database.connector.DTO;
import de.xahrie.trues.api.database.query.Condition;
import de.xahrie.trues.api.database.query.JoinQuery;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.riot.game.Game;
import de.xahrie.trues.api.riot.performance.TeamPerf;
import de.xahrie.trues.api.util.Format;
import de.xahrie.trues.api.util.StringUtils;
import lombok.Getter;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;

@ExtensionMethod(StringUtils.class)
@Getter
public final class LeagueTableDTO implements DTO<LeagueTableDTO> {
  private final LeagueTeam leagueTeam;
  private List<Object> data;

  public LeagueTableDTO(LeagueTeam leagueTeam) {
    this.leagueTeam = leagueTeam;
  }

  public Query<TeamPerf> query() {
    final int teamId = leagueTeam.getTeam().getId();
    return new Query<>(TeamPerf.class)
        .get("concat(round(avg(if(_teamperf.team = " + teamId + ", _teamperf.kills, null))), ':', round(avg(if(_teamperf.team = " + teamId + ", null, _teamperf.kills))))", String.class)
        .get("sum(if(_teamperf.team = " + teamId + ", _teamperf.kills, -_teamperf.kills))", String.class)
        .get("concat(round(avg(if(_teamperf.team = " + teamId + ", _teamperf.total_gold, null)) / 1000), ':', round(avg(if(_teamperf.team = " + teamId + ", null, _teamperf.total_gold)) / 1000))", String.class)
        .get("round(sum(if(_teamperf.team = " + teamId + ", _teamperf.total_gold, -_teamperf.total_gold)) / 1000)", String.class)
        .get("concat(round(avg(if(_teamperf.team = " + teamId + ", _teamperf.total_damage, null)) / 1000), ':', round(avg(if(_teamperf.team = " + teamId + ", null, _teamperf.total_damage)) / 1000))", String.class)
        .get("round(sum(if(_teamperf.team = " + teamId + ", _teamperf.total_damage, -_teamperf.total_damage)) / 1000)", String.class)
        .get("concat(round(avg(if(_teamperf.team = " + teamId + ", _teamperf.total_creeps, null))), ':', round(avg(if(_teamperf.team = " + teamId + ", null, _teamperf.total_creeps))))", String.class)
        .get("sum(if(_teamperf.team = " + teamId + ", _teamperf.total_creeps, -_teamperf.total_creeps))", String.class)
        .get("concat(round(avg(if(_teamperf.team = " + teamId + ", _teamperf.total_vision, null))), ':', round(avg(if(_teamperf.team = " + teamId + ", null, _teamperf.total_vision))))", String.class)
        .get("sum(if(_teamperf.team = " + teamId + ", _teamperf.total_vision, -_teamperf.total_vision))", String.class)
        .get("concat(sum(if(_teamperf.team = " + teamId + ", _teamperf.turrets, 0)), ' ', " +
            "sum(if(_teamperf.team = " + teamId + ", _teamperf.drakes, 0)), ' ', " +
            "sum(if(_teamperf.team = " + teamId + ", _teamperf.inhibs, 0)), ' ', " +
            "sum(if(_teamperf.team = " + teamId + ", _teamperf.heralds, 0)), ' ', " +
            "sum(if(_teamperf.team = " + teamId + ", _teamperf.barons, 0)))", String.class)
        .get("concat(floor(avg(_game.duration) / 60), ':', lpad(avg(_game.duration) % 60, 2, '0'))", String.class)
        .join(TeamPerf.class, Game.class)
        .where(Condition.inSubquery("_game.coverage",
            new Query<>(Participator.class).get("_participator.coverage", Integer.class)
                                           .join(new JoinQuery<>(Participator.class, Match.class).col("coverage"))
                                           .where("_participator.team", teamId).and("_match.coverage_group", leagueTeam.getLeague()).and(Condition.Comparer.NOT_EQUAL, "_match.result", "0:0")));
  }

  @Override
  public List<Object> getData() {
    if (data == null) {
      this.data = new ArrayList<>();
      data.add(leagueTeam.getScore().place() + ".");
      data.add(leagueTeam.getTeam().getName());
      data.add(leagueTeam.getScore().isDisqualified() ? "dq" : leagueTeam.getScore().getStanding().format(Format.ADDITIONAL));
      final List<Object> elements = Arrays.stream(query().single(List.of(leagueTeam.getTeam(), leagueTeam.getLeague()))).toList();
      for (int i = 0; i < elements.size(); i++) {
        final Object element = elements.get(i);
        if (element == null) data.add("?");
        else if (element instanceof BigDecimal decimal) data.add((decimal.intValue() > 0 && i < 10 ? "+" : "") + decimal);
        else if (element instanceof String string) data.add(string);
        else throw new IllegalArgumentException("Der Typ ist nicht bekannt.");
      }
    }
    return data;
  }

  public Integer getDifference(Stat stat) {
    return getInt(stat.ordinal() * 2 + 4);
  }

  public Standing getStanding(Stat stat) {
    final String string = getString(stat.ordinal() * 2 + 3);
    return new Standing(string.before(":").intValue(), string.after(":").intValue());
  }

  @Override
  public int compareTo(@NotNull LeagueTableDTO o) {
    return Comparator.comparing(LeagueTableDTO::getLeagueTeam)
        .thenComparing(leagueTableDTO -> leagueTableDTO.getDifference(Stat.KILLS), Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(leagueTableDTO -> leagueTableDTO.getDifference(Stat.GOLD), Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(leagueTableDTO -> leagueTableDTO.getDifference(Stat.DAMAGE), Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(leagueTableDTO -> leagueTableDTO.getDifference(Stat.CREEPS), Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(leagueTableDTO -> leagueTableDTO.getDifference(Stat.VISION), Comparator.nullsLast(Comparator.naturalOrder()))
        .compare(this, o);
  }


  public enum Stat {
    KILLS,
    GOLD,
    DAMAGE,
    CREEPS,
    VISION
  }
}
