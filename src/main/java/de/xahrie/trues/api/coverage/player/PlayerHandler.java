package de.xahrie.trues.api.coverage.player;

import java.io.Serial;
import java.io.Serializable;

import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.coverage.player.model.PlayerRank;
import de.xahrie.trues.api.coverage.player.model.Rank;
import lombok.Builder;
import lombok.Getter;
import no.stelar7.api.r4j.basic.constants.types.lol.GameQueueType;
import no.stelar7.api.r4j.pojo.lol.league.LeagueEntry;
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner;

@Getter
public class PlayerHandler extends PlayerModel implements Serializable {
  @Serial
  private static final long serialVersionUID = -3900511589414972005L;

  @Builder
  public PlayerHandler(String url, Player player) {
    super(url, player);
  }

  public void updateName() {
    player.setSummonerName(player.getRiotUser().updateName());
  }

  /**
   * Aktualisiere Rang eines Spielers in der Datenbank
   */
  public PlayerRank updateElo() {
    final Summoner summoner = player.getRiotUser().getSummoner();
      LeagueEntry entry = null;
      if (summoner != null)
        entry = summoner.getLeagueEntry().stream()
            .filter(leagueEntry -> leagueEntry.getQueueType().equals(GameQueueType.RANKED_SOLO_5X5))
            .findFirst().orElse(null);
      if (entry == null)
        return player.getRanks().getCurrent();

      final String tier = entry.getTier();
      final Rank.Division division = Rank.Division.valueOf(entry.getRank());
      final int leaguePoints = entry.getLeaguePoints();
      final int wins = entry.getWins();
      final int losses = entry.getLosses();
      return player.getRanks().updateRank(Rank.RankTier.valueOf(tier), division, (short) leaguePoints, wins, losses);
  }
}
