package de.xahrie.trues.api.coverage.season.signup;

import java.util.NoSuchElementException;

import de.xahrie.trues.api.coverage.season.PRMSeason;
import de.xahrie.trues.api.coverage.season.SeasonFactory;
import de.xahrie.trues.api.coverage.team.model.PRMTeam;
import de.xahrie.trues.api.util.io.log.DevInfo;
import lombok.NonNull;

public class SignupFactory {
  @NonNull
  public static SeasonSignup create(PRMTeam team, String info) {
    final PRMSeason upcomingPRMSeason = SeasonFactory.getUpcomingPRMSeason();
    if (upcomingPRMSeason == null) {
      final RuntimeException exception = new NoSuchElementException("Die letzte Season wurde nicht gefunden.");
      new DevInfo().warn(exception);
      throw exception;
    }

    final SeasonSignup signup = team.getSignupForSeason(upcomingPRMSeason);
    return signup != null ? signup : new SeasonSignup(upcomingPRMSeason, team, info).create();
  }
}
