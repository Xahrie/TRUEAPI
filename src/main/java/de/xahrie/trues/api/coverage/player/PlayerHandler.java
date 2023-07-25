package de.xahrie.trues.api.coverage.player;

import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.coverage.player.model.PlayerRank;
import de.xahrie.trues.api.coverage.player.model.Rank;
import de.xahrie.trues.api.riot.Zeri;
import lombok.Builder;
import lombok.Getter;
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard;
import no.stelar7.api.r4j.basic.constants.types.lol.GameQueueType;
import no.stelar7.api.r4j.pojo.lol.league.LeagueEntry;
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner;

import java.io.Serial;
import java.io.Serializable;

@Getter
public class PlayerHandler extends PlayerModel implements Serializable {
  @Serial
  private static final long serialVersionUID = -3900511589414972005L;

  @Builder
  public PlayerHandler(String url, Player player) {
    super(url, player);
  }

  public void updateName() {
    final Summoner summoner = Zeri.get().getSummonerAPI().getSummonerByPUUID(LeagueShard.EUW1, player.getPuuid());
    if (summoner != null) player.setSummonerName(summoner.getName());
  }

  public PlayerRank updateElo() {
    final Summoner summoner = Zeri.get().getSummonerAPI().getSummonerByPUUID(LeagueShard.EUW1, player.getPuuid());
      LeagueEntry entry = null;
      for (LeagueEntry leagueEntry1 : summoner.getLeagueEntry()) {
        if (leagueEntry1.getQueueType().equals(GameQueueType.RANKED_SOLO_5X5)) {
          entry = leagueEntry1;
          break;
        }
      }
      if (entry == null) return player.getRanks().getCurrent();

      final String tier = entry.getTier();
      final Rank.Division division = Rank.Division.valueOf(entry.getRank());
      final int leaguePoints = entry.getLeaguePoints();
      final int wins = entry.getWins();
      final int losses = entry.getLosses();
      return player.getRanks().updateRank(Rank.RankTier.valueOf(tier), division, (short) leaguePoints, wins, losses);


  }
}
