package de.xahrie.trues.api.riot.api;

import java.util.List;

import de.xahrie.trues.api.datatypes.collections.SortedList;
import de.xahrie.trues.api.riot.Zeri;
import de.xahrie.trues.api.util.StringUtils;
import lombok.experimental.ExtensionMethod;
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard;
import no.stelar7.api.r4j.basic.constants.api.regions.RegionShard;
import no.stelar7.api.r4j.pojo.lol.clash.ClashTournament;
import no.stelar7.api.r4j.pojo.lol.match.v5.LOLMatch;
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner;
import no.stelar7.api.r4j.pojo.shared.RiotAccount;
import org.jetbrains.annotations.Nullable;

@ExtensionMethod(StringUtils.class)
public class LolApi {
  private final Zeri api;

  public LolApi(Zeri api) {
    this.api = api;
  }

  public RiotUser getUserFromPuuid(String puuid) {
    return new RiotUser(puuid, null);
  }

  public RiotUser getUserFromName(String name) {
    return getUserFromName(RiotName.of(name));
  }

  public RiotUser getUserFromName(String name, String tag) {
    return getUserFromName(RiotName.of(name, tag));
  }

  public RiotUser getUserFromName(RiotName riotName) {
    return new RiotUser(null, riotName);
  }

  RiotAccount getAccountFromPuuid(String puuid) {
    if (puuid == null) return null;
    return api.getAccountAPI().getAccountByPUUID(RegionShard.EUROPE, puuid);
  }

  RiotAccount getAccountFromName(RiotName name) {
    if (name == null) return null;
    return api.getAccountAPI().getAccountByTag(RegionShard.EUROPE, name.getName(), name.getTag());
  }

  Summoner getSummonerFromPuuid(String puuid) {
    if (puuid == null) return null;
    return api.getLoLAPI().getSummonerAPI().getSummonerByPUUID(LeagueShard.EUW1, puuid);
  }

  Summoner getSummonerByName(@Nullable RiotName name) {
    if (name == null) return null;

    final String riotId = name.toString();
    if (riotId == null) return null;
    return api.getLoLAPI().getSummonerAPI().getSummonerByName(LeagueShard.EUW1, riotId);
  }

  public SortedList<ClashTournament> getTournaments() {
    final List<ClashTournament> tournaments = api.getLoLAPI().getClashAPI().getTournaments(LeagueShard.EUW1);
    return SortedList.of(tournaments);
  }

  public LOLMatch getMatch(String matchId) {
    return api.getLoLAPI().getMatchAPI().getMatch(RegionShard.EUROPE, matchId);
  }
}
