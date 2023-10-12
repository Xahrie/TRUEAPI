package de.xahrie.trues.api.riot.match;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import de.xahrie.trues.api.riot.Zeri;
import no.stelar7.api.r4j.basic.constants.types.lol.GameQueueType;
import no.stelar7.api.r4j.basic.constants.types.lol.MatchlistMatchType;
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner;

public record MatchHistoryBuilder(Summoner summoner, LocalDateTime start, List<String> get) {
  public MatchHistoryBuilder(Summoner summoner, LocalDateTime start) {
    this(summoner, start, new ArrayList<>());
  }

  public MatchHistoryBuilder all() {
    handleMatchList(null, null);
    return this;
  }

  public MatchHistoryBuilder with(GameQueueType queueType) {
    handleMatchList(queueType, null);
    return this;
  }

  public MatchHistoryBuilder with(MatchlistMatchType matchType) {
    handleMatchList(null, matchType);
    return this;
  }

  private void handleMatchList(GameQueueType queueType, MatchlistMatchType matchType) {
    final Long startEpoch = start == null ? null : start.atZone(ZoneId.systemDefault()).toEpochSecond();
    int start = 0;
    while (true) {
      final List<String> matchList = Zeri.lol().getMatchIds(summoner, queueType, matchType, start, startEpoch);
      get.addAll(matchList);
      if (matchList.size() < 100) break;
      start += 100;
    }
  }

  @Override
  public List<String> get() {
    return get.stream().sorted().toList();
  }
}
