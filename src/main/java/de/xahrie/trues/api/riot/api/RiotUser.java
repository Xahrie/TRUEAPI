package de.xahrie.trues.api.riot.api;

import java.time.LocalDateTime;
import java.time.ZoneId;

import de.xahrie.trues.api.datatypes.collections.SortedList;
import de.xahrie.trues.api.riot.Zeri;
import de.xahrie.trues.api.util.StringUtils;
import de.xahrie.trues.api.util.Util;
import lombok.experimental.ExtensionMethod;
import no.stelar7.api.r4j.basic.constants.types.lol.GameQueueType;
import no.stelar7.api.r4j.basic.constants.types.lol.MatchlistMatchType;
import no.stelar7.api.r4j.pojo.lol.championmastery.ChampionMastery;
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner;
import no.stelar7.api.r4j.pojo.shared.RiotAccount;

@ExtensionMethod(StringUtils.class)
public class RiotUser {
  RiotUser(String puuid, RiotName name) {
    this.puuid = puuid;
    this.name = name;
  }

  private String puuid;
  private RiotName name;

  private RiotAccount account;
  private Summoner summoner;

  public RiotAccount getAccount() {
    if (account == null) {
      if (puuid != null) {
        this.account = Zeri.lol().getAccountFromPuuid(puuid);
        if (account != null)
          this.name = RiotName.of(account.getName(), account.getTag());
      } else if (name.getTag() == null) {
        getSummoner();
        if (puuid != null)
          this.account = Zeri.lol().getAccountFromPuuid(puuid);
      } else
        this.account = Zeri.lol().getAccountFromName(name);
      if (puuid == null && account != null)
        this.puuid = account.getPUUID();
    }
    return account;
  }

  public Summoner getSummoner() {
    if (summoner == null) {
      if (puuid != null)
        this.summoner = Zeri.lol().getSummonerFromPuuid(puuid);
      else if (name.getTag() != null) {
        getAccount();
        if (puuid != null)
          this.summoner = Zeri.lol().getSummonerByName(name);
      } else
        this.summoner = Zeri.lol().getSummonerByName(name);
      if (puuid == null && summoner != null)
        this.puuid = summoner.getPUUID();
    }
    return summoner;
  }

  public String getPUUID() {
    if (puuid == null)
      this.puuid = getAccount().getPUUID();
    return puuid;
  }

  public RiotName updateName() {
    if (getAccount() != null)
      this.name = RiotName.of(getAccount());
    return name;
  }

  public RiotName getName() {
    if (name == null || name.getTag() == null)
      this.name = RiotName.of(getAccount());
    return name;
  }

  public String getSummonerId() {
    return Util.avoidNull(getSummoner(), Summoner::getSummonerId);
  }

  public boolean exists() {
    return getAccount() != null || getSummoner() != null;
  }

  public SortedList<ChampionMastery> getMastery() {
    return Util.avoidNull(getSummoner(), SortedList.of(), summoner -> SortedList.of(summoner.getChampionMasteries()));
  }

  public SortedList<String> getMatchIds(GameQueueType queueType, MatchlistMatchType matchType, Integer start,
                                        Long startEpoch) {
    final Long endEpoch = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
    return getMatchIds(queueType, matchType, start, startEpoch, endEpoch);
  }

  public SortedList<String> getMatchIds(GameQueueType queueType, MatchlistMatchType matchType, Integer start,
                                        Long startEpoch, Long endEpoch) {
    return SortedList.of(
        getSummoner().getLeagueGames()
            .withQueue(queueType).withType(matchType)
            .withBeginIndex(start).withCount(100)
            .withStartTime(startEpoch).withEndTime(endEpoch)
            .get()
    );
  }
}
