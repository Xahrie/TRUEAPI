package de.xahrie.trues.api.riot.clash;


import java.time.Duration;
import java.time.LocalDateTime;

import de.xahrie.trues.api.coverage.season.Season;
import de.xahrie.trues.api.database.query.Condition;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.riot.Zeri;
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard;
import no.stelar7.api.r4j.pojo.lol.clash.ClashTournament;
import no.stelar7.api.r4j.pojo.lol.clash.ClashTournamentPhase;

public class ClashLoader {
  private static final Duration CLASH_DELAY = Duration.ofMinutes(45);
  private static final Duration CLASH_DURATION = Duration.ofMinutes(345);
  public static void loadAllClashes() {
    for (ClashTournament tournament : Zeri.get().getClashAPI().getTournaments(LeagueShard.EUW1)) {
      final String name =  tournament.getNameKey();
      for (ClashTournamentPhase phase : tournament.getSchedule()) {
        final LocalDateTime registration = phase.getRegistrationTimeAsDate().toLocalDateTime();
        final LocalDateTime start = phase.getStartTimeAsDate().toLocalDateTime();
        final var season = new Query<>(Season.class).where(Condition.Comparer.SMALLER_EQUAL, "season_start", start)
                                                    .descending("season_start").entity();
        new Clash((short) phase.getId(), season, name, registration, start, phase.isCancelled()).create();
      }

    }
  }

  public static boolean isClashActive() {
    final LocalDateTime now = LocalDateTime.now();
    for (final Clash clash : new Query<>(Clash.class).where("active", true).entityList()) {
      final LocalDateTime start = clash.getClashStart().plus(CLASH_DELAY);
      final LocalDateTime end = clash.getClashStart().plus(CLASH_DURATION);
      if (start.isAfter(now) || end.isBefore(now)) continue;

      return true;
    }
    return false;
  }
}
