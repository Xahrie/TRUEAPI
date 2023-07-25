package de.xahrie.trues.api.coverage.match;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import de.xahrie.trues.api.coverage.match.model.Match;
import de.xahrie.trues.api.coverage.match.model.PRMMatch;
import de.xahrie.trues.api.coverage.match.model.Scrimmage;
import de.xahrie.trues.api.coverage.participator.model.Participator;
import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import de.xahrie.trues.api.database.query.Condition;
import de.xahrie.trues.api.database.query.JoinQuery;
import de.xahrie.trues.api.database.query.Query;
import org.jetbrains.annotations.Nullable;

public final class MatchFactory {
  public static List<Scrimmage> getUpcomingScrims() {
    return new Query<>(Scrimmage.class).where("result", "-:-").ascending("coverage_start").entityList();
  }

  public static List<Match> getMatchesOf(AbstractTeam team, AbstractTeam opponent) {
    return new Query<>(Match.class, "SELECT c.* FROM coverage c WHERE ? in (SELECT team FROM coverage_team WHERE coverage = c.coverage_id) AND ? in (SELECT team FROM coverage_team WHERE coverage = c.coverage_id)").entityList(List.of(team, opponent));
  }

  @Nullable
  public static PRMMatch getMatch(int matchId) {
    final PRMMatch match = new Query<>(PRMMatch.class).where("match_id", matchId).entity();
    if (match != null) return match;

    return new MatchLoader(matchId).create().getMatch();
  }

  public static List<AbstractTeam> getNextTeams() {
    return new Query<>(Participator.class)
            .join(new JoinQuery<>(Participator.class, Match.class).col("coverage"))
            .where(Condition.Comparer.GREATER_EQUAL, "_match.coverage_start", LocalDateTime.now()).or("result", "-:-")
            .or("result", "-:-")
            .ascending("_match.coverage_start").convertList(Match.class)
            .stream().filter(Match::isOrgagame)
            .flatMap(nextOrgaMatch -> Arrays.stream(nextOrgaMatch.getParticipators()))
            .map(Participator::getTeam).distinct().toList();
  }
}
