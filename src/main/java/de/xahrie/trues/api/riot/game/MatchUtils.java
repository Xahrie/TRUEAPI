package de.xahrie.trues.api.riot.game;

import java.time.LocalDateTime;

import no.stelar7.api.r4j.basic.constants.types.lol.GameQueueType;
import no.stelar7.api.r4j.pojo.lol.match.v5.LOLMatch;

public class MatchUtils {
  public static GameType getGameQueue(LOLMatch match) {
    if (match.getQueue().equals(GameQueueType.CUSTOM)) return match.getTournamentCode().isEmpty() ? GameType.CUSTOM : GameType.TOURNAMENT;
    else return GameType.fromQueueType(match.getQueue());
  }

  public static String getMatchId(LOLMatch match) {
    return match.getPlatform().getValue() + "_" + match.getGameId();
  }

  public static LocalDateTime getCreation(LOLMatch match) {
    return match.getMatchCreationAsDate().toLocalDateTime();
  }
}
