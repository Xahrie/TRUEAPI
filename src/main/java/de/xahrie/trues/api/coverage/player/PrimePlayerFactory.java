package de.xahrie.trues.api.coverage.player;

import de.xahrie.trues.api.coverage.player.model.PRMPlayer;
import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.database.query.Condition;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.riot.Zeri;
import de.xahrie.trues.api.util.Util;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner;
import org.jetbrains.annotations.Nullable;

@ExtensionMethod(Util.class)
public final class PrimePlayerFactory {
  /**
   * @return Spieler von der Primeid
   */
  @Nullable
  public static PRMPlayer getPlayer(int playerId) {
    final PRMPlayer prmPlayer = new Query<>(PRMPlayer.class).where("prm_id", playerId).entity();
    if (prmPlayer != null) return prmPlayer;

    return PlayerLoader.create(playerId).avoidNull(PlayerLoader::getPlayer);
  }


  @Nullable
  public static PRMPlayer getPrimePlayer(int primeId, String summonerName) {
    PRMPlayer player = new Query<>(PRMPlayer.class).where("prm_id", primeId).entity();
    if (summonerName == null)
      return player;

    if (player != null)
      updatePrmAccount(player, summonerName);
    else {
      player = createPlayer(summonerName, primeId);
      if (player != null) player.setPrmUserId(primeId);
    }
    return player;
  }

  private static void updatePrmAccount(@NonNull PRMPlayer player, @NonNull String summonerName) {
    final Summoner summoner = Zeri.lol().getSummonerByName(summonerName);
    final String puuid = summoner == null ? player.getPuuid() : summoner.getPUUID();
    final String summonerId = summoner == null ? player.getSummonerId() : summoner.getSummonerId();
    final String name = summoner == null ? player.getSummonerName() : summoner.getName();

    final Player p = new Query<>(Player.class).where("lol_puuid", puuid)
                                              .and(Condition.Comparer.NOT_EQUAL, "player_id", player.getId()).entity();
    if (p != null)
      p.setPuuidAndName(null, null, null);

    if (player.getPuuid() == null) {
      if (puuid != null) player.setPuuidAndName(puuid, summonerId, name);
      return;
    }
    if (!player.getPuuid().equals(puuid) && puuid != null)
      player.setPuuidAndName(puuid, summonerId, name);
  }

  @Nullable
  private static PRMPlayer createPlayer(String summonerName, int primeId) {
    final Summoner summoner = Zeri.lol().getSummonerByName(summonerName);
    if (summoner != null) {
      final String puuid = summoner.getPUUID();
      final String summonerId = summoner.getSummonerId();
      if (puuid != null) return new PRMPlayer(summonerName, puuid, summonerId, primeId).create();
    }

    final PRMPlayer prmPlayer = (PRMPlayer) performNoPuuid(summonerName);
    if (prmPlayer != null) {
      prmPlayer.setPrmUserId(primeId);
    }
    return prmPlayer;
  }

  @Nullable
  private static Player performNoPuuid(String summonerName) {
    final Player player = determineExistingPlayerFromName(summonerName);
    if (player == null) return null;

    new PlayerHandler(null, player).updateName();
    return determineExistingPlayerFromName(summonerName);
  }

  private static Player determineExistingPlayerFromName(String summonerName) {
    return summonerName == null ? null : new Query<>(Player.class).where("lol_name", summonerName).entity();
  }

  private static Player determineExistingPlayerFromPuuid(String puuid) {
    return puuid == null ? null : new Query<>(Player.class).where("lol_puuid", puuid).entity();
  }
}
