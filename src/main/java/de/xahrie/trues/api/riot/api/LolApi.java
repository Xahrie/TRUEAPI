package de.xahrie.trues.api.riot.api;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.datatypes.collections.SortedList;
import de.xahrie.trues.api.riot.Zeri;
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard;
import no.stelar7.api.r4j.basic.constants.api.regions.RegionShard;
import no.stelar7.api.r4j.basic.constants.types.lol.GameQueueType;
import no.stelar7.api.r4j.basic.constants.types.lol.MatchlistMatchType;
import no.stelar7.api.r4j.pojo.lol.championmastery.ChampionMastery;
import no.stelar7.api.r4j.pojo.lol.clash.ClashTournament;
import no.stelar7.api.r4j.pojo.lol.match.v5.LOLMatch;
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner;

public class LolApi {
  private final Zeri api;

  public LolApi(Zeri api) {
    this.api = api;
  }

  public Summoner getSummonerByPlayer(Player player) {
    if (player == null) return null;

    Summoner summoner = getSummonerByPuuid(player.getPuuid());
    if (summoner == null) summoner = getSummonerByName(player.getSummonerName());
    return summoner;
  }

  public Summoner getSummonerByPuuid(String puuid) {
    if (puuid == null) return null;
    return api.getLoLAPI().getSummonerAPI().getSummonerByPUUID(LeagueShard.EUW1, puuid);
  }

  public Summoner getSummonerByName(String summonerName) {
    if (summonerName == null) return null;
    return api.getLoLAPI().getSummonerAPI().getSummonerByPUUID(LeagueShard.EUW1, summonerName);
  }

  public SortedList<ChampionMastery> getMastery(Player player) {
    final Summoner summonerByPlayer = getSummonerByPlayer(player);
    if (summonerByPlayer == null) return SortedList.of();
    final String summonerId = summonerByPlayer.getSummonerId();
    final List<ChampionMastery> championMasteries = api.getLoLAPI().getMasteryAPI()
        .getChampionMasteries(LeagueShard.EUW1, summonerId);
    return SortedList.of(championMasteries);
  }

  public SortedList<ClashTournament> getTournaments() {
    final List<ClashTournament> tournaments = api.getLoLAPI().getClashAPI().getTournaments(LeagueShard.EUW1);
    return SortedList.of(tournaments);
  }

  public SortedList<String> getMatchIds(Summoner summoner, GameQueueType queueType, MatchlistMatchType matchType,
                                  Integer start, Long startEpoch) {
    final Long endEpoch = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
    return getMatchIds(summoner, queueType, matchType, start, startEpoch, endEpoch);
  }

  public SortedList<String> getMatchIds(Summoner summoner, GameQueueType queueType, MatchlistMatchType matchType,
                                  Integer start, Long startEpoch, Long endEpoch) {
    final List<String> matchIds = api.getLoLAPI().getMatchAPI()
        .getMatchList(RegionShard.EUROPE, summoner.getPUUID(), queueType, matchType, start, 100,
            startEpoch, endEpoch);
    return SortedList.of(matchIds);
  }

  public LOLMatch getMatch(String matchId) {
    return api.getLoLAPI().getMatchAPI().getMatch(RegionShard.EUROPE, matchId);
  }
}
