package de.xahrie.trues.api.coverage.match;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import de.xahrie.trues.api.coverage.match.model.Match;
import de.xahrie.trues.api.coverage.participator.model.Participator;
import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import de.xahrie.trues.api.database.query.Condition;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.collections.SortedList;

public final class UpcomingDataFactory {
  private static UpcomingDataFactory instance;

  public static UpcomingDataFactory getInstance() {
    if (instance == null) refresh();
    return instance;
  }

  public static void refresh() {
    instance = new UpcomingDataFactory();
  }

  private final List<Match> nextMatches;

  private UpcomingDataFactory() {
    this.nextMatches = getMatches(3);
  }

  public List<AbstractTeam> getTeams() {
    return SortedList.of(nextMatches.stream().flatMap(match -> Arrays.stream(match.getParticipators())).map(
            Participator::getTeam));
  }

  public List<Match> getMatches() {
    return nextMatches;
  }

  public static List<Match> getMatches(int hours) {
    if (hours == 3 && instance != null)
      return getInstance().getMatches();

    final LocalDateTime timestamp = LocalDateTime.now().plusHours(hours);
    return new Query<>(Match.class).where(Condition.Comparer.SMALLER_EQUAL, "coverage_start", timestamp)
        .and("result", "-:-").ascending("coverage_start").entityList();
  }
}
