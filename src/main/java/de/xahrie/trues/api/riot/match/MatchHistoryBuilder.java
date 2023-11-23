package de.xahrie.trues.api.riot.match;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import de.xahrie.trues.api.riot.api.RiotUser;
import no.stelar7.api.r4j.basic.constants.types.lol.GameQueueType;
import no.stelar7.api.r4j.basic.constants.types.lol.MatchlistMatchType;

public record MatchHistoryBuilder(RiotUser user, LocalDateTime start, List<String> get) {

  public MatchHistoryBuilder(RiotUser user, LocalDateTime start) {
    this(user, start, new ArrayList<>());
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
      final List<String> matchList = user.getMatchIds(queueType, matchType, start, startEpoch);
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
