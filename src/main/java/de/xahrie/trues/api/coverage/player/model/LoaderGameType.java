package de.xahrie.trues.api.coverage.player.model;

import java.util.List;

import de.xahrie.trues.api.riot.match.MatchHistoryBuilder;
import no.stelar7.api.r4j.basic.constants.types.lol.GameQueueType;
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner;

public enum LoaderGameType {
  CLASH_PLUS,
  TOURNAMENT,
  CLASH,
  MATCHMADE;

  public List<String> getMatchHistory(Summoner summoner, Player player) {
    final var mh = new MatchHistoryBuilder(summoner, player.getUpdated());
    return (switch (this) {
      case MATCHMADE -> mh.all();
      case CLASH -> mh.with(GameQueueType.CLASH);
      case TOURNAMENT -> mh.with(GameQueueType.CUSTOM);
      case CLASH_PLUS -> mh.with(GameQueueType.CLASH).with(GameQueueType.CUSTOM);
    }).get();
  }
}
