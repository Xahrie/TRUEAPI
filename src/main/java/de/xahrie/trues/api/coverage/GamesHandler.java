package de.xahrie.trues.api.coverage;

import de.xahrie.trues.api.coverage.match.MatchLoader;

public final class GamesHandler {
  public static int loadGames(int start, int ende, int division, int rest, int startValue) {
    if (startValue > ende) return 0;
    start = Math.max(start, startValue);
    int count = 0;
    for (int i = start; i <= ende; i++) {
      if (i % division != rest) continue;
      new MatchLoader(i).loadParticipatingPlayers();
      count++;
    }
    return count;
  }
}
