package de.xahrie.trues.api.coverage.match.model;

import java.time.Duration;

import de.xahrie.trues.api.database.connector.Listing;

@Listing(Listing.ListingType.ORDINAL)
public enum MatchFormat {
  NO_GAMES,
  ONE_GAME, // 42:30
  TWO_GAMES, // 1:30:00
  BEST_OF_THREE, // 2:17:30
  FOUR_GAMES, // 3:05:00
  BEST_OF_FIVE, // 3:52:30
  SIX_GAMES,
  BEST_OF_SEVEN,
  EIGHT_GAMES,
  BEST_OF_NINE;

  public Duration getPRMDuration() {
    return getDuration(2, 2, 9.9892, 29.7047, .7426, 5);
  }

  public Duration getDuration(double initialLobby, double lobby, double preGame, double game, double gamePause, double pause) {
    if (ordinal() == 0) return Duration.ZERO;
    final double durationPerGame = lobby + preGame + game + gamePause + pause;
    final double totalDuration = initialLobby + ordinal() * durationPerGame - pause; // keine Pause am Ende
    return Duration.ofMinutes(Math.round(totalDuration));
  }
}
