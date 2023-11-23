package de.xahrie.trues.api.coverage.player;

import de.xahrie.trues.api.coverage.player.model.PRMPlayer;
import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.database.query.Condition;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.riot.Zeri;
import de.xahrie.trues.api.riot.api.RiotName;
import de.xahrie.trues.api.riot.api.RiotUser;
import de.xahrie.trues.api.util.Util;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
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
  public static PRMPlayer getPrimePlayer(int primeId, RiotName name) {
    PRMPlayer player = new Query<>(PRMPlayer.class).where("prm_id", primeId).entity();
    if (name == null)
      return player;

    if (player != null)
      updatePrmAccount(player, name);
    else {
      player = createPlayer(name, primeId);
      if (player != null) player.setPrmUserId(primeId);
    }
    return player;
  }

  private static void updatePrmAccount(@NonNull PRMPlayer player, @NonNull RiotName name) {
    final RiotUser riotUser = Zeri.lol().getUserFromName(name);
    final String puuid = Util.avoidNull(riotUser.getPUUID(), player.getPuuid());
    final String summonerId = Util.avoidNull(player.getSummonerId(), player.getSummonerId());

    final Player p = new Query<>(Player.class).where("lol_puuid", puuid)
        .and(Condition.Comparer.NOT_EQUAL, "player_id", player.getId()).entity();
    if (p != null)
      p.setPuuidAndName(null, null, RiotName.of(null, null));

    name = Util.avoidNull(riotUser.updateName(), player.getName());
    if (player.getPuuid() == null) {
      if (puuid != null) player.setPuuidAndName(puuid, summonerId, name);
      return;
    }
    if (!player.getPuuid().equals(puuid) && puuid != null)
      player.setPuuidAndName(puuid, summonerId, name);
  }

  @Nullable
  private static PRMPlayer createPlayer(RiotName name, int primeId) {
    final RiotUser riotUser = Zeri.lol().getUserFromName(name);
    final String puuid = riotUser.getPUUID();
    final String summonerId = riotUser.getSummonerId();
    if (puuid != null && summonerId != null)
      return new PRMPlayer(name, puuid, summonerId, primeId).create();

    final PRMPlayer prmPlayer = (PRMPlayer) performNoPuuid(name);
    if (prmPlayer != null) {
      prmPlayer.setPrmUserId(primeId);
    }
    return prmPlayer;
  }

  @Nullable
  private static Player performNoPuuid(RiotName name) {
    final Player player = determineExistingPlayerFromName(name);
    if (player == null) return null;

    new PlayerHandler(null, player).updateName();
    return determineExistingPlayerFromName(name);
  }

  private static Player determineExistingPlayerFromName(RiotName name) {
    if (name == null) return null;
    return new Query<>(Player.class).where("lol_name", name.getName()).and("lol_tag", name.getTag())
        .entity();
  }

  private static Player determineExistingPlayerFromPuuid(String puuid) {
    return puuid == null ? null : new Query<>(Player.class).where("lol_puuid", puuid).entity();
  }
}
