package de.xahrie.trues.api.coverage.match.model;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import de.xahrie.trues.api.coverage.participator.model.Participator;
import de.xahrie.trues.api.coverage.match.log.EventStatus;
import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import de.xahrie.trues.api.database.connector.DTO;
import de.xahrie.trues.api.database.query.Condition;
import de.xahrie.trues.api.database.query.JoinQuery;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.TimeFormat;
import de.xahrie.trues.api.util.Util;
import org.jetbrains.annotations.NotNull;

public record MatchOfTeamDTO(Match match, AbstractTeam team) implements DTO<MatchOfTeamDTO> {
  public static Query<Participator> get(AbstractTeam team) {
    return get(team, LocalDateTime.now().minusDays(180));
  }

  public static Query<Participator> get(AbstractTeam team, LocalDateTime start) {
    return new Query<>(Participator.class).get("coverage", Match.class).get("team", AbstractTeam.class)
        .join(new JoinQuery<>(Participator.class, Match.class).col("coverage"))
        .keep("team", team)
        .where(Condition.Comparer.GREATER_EQUAL, "_match.coverage_start", start).or("_match.status", EventStatus.PLAYED)
        .ascending("_match.coverage_start");
  }

  @Override
  public List<Object> getData() {
    return List.of(
        TimeFormat.DISCORD.of(match.getStart()),
        Util.avoidNull(match.getOpponentOf(team), "keine Gegner", AbstractTeam::getName),
        match.getResult().toString()
    );
  }

  @Override
  public int compareTo(@NotNull MatchOfTeamDTO o) {
    return Comparator.comparing(MatchOfTeamDTO::match).compare(this, o);
  }
}
