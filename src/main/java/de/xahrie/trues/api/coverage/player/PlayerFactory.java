package de.xahrie.trues.api.coverage.player;

import java.util.List;

import de.xahrie.trues.api.coverage.player.model.PRMPlayer;
import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.coverage.player.model.PlayerImpl;
import de.xahrie.trues.api.database.query.Condition;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.riot.Zeri;
import de.xahrie.trues.api.riot.api.RiotUser;
import de.xahrie.trues.api.util.io.log.Console;
import de.xahrie.trues.api.util.io.log.DevInfo;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public final class PlayerFactory {
  @NonNull
  public static List<Player> registeredPlayers() {
    return new Query<>(Player.class).where(Condition.notNull("discord_user")).entityList();
  }

  @Nullable
  public static Player findPlayer(@Nullable String puuid) {
    if (puuid == null) return null;
    return new Query<>(Player.class).where("lol_puuid", puuid).entity();
  }

  @Nullable
  public static Player getPlayerFromPuuid(String puuid) {
    Player player = findPlayer(puuid);
    if (player == null) {
      final RiotUser user = Zeri.lol().getUserFromPuuid(puuid);
      if (user.exists())
        player = new PlayerImpl(user.updateName(), user.getSummonerId(), puuid).create();
    }
    return player;
  }

  @Nullable
  public static Player getPlayerFromName(String summonerName) {
    final Player player = lookForPlayer(summonerName);
    if (player != null)
      return player;

    RiotUser user = Zeri.lol().getUserFromName(summonerName);
    if (!user.exists()) {
      new DevInfo("Der Spieler **" + summonerName + "** existiert nicht").with(Console.class).warn();
      return null;
    }

    return new PlayerImpl(user.getName(), user.getSummonerId(), user.getPUUID()).create();
  }

  private static Player lookForPlayer(String summonerName) {
    final Player player = determineExistingPlayerFromName(summonerName);
    if (player != null)
      return player;

    RiotUser user = Zeri.lol().getUserFromName(summonerName);
    return findPlayer(user.getPUUID());
  }

  @Nullable
  private static Player determineExistingPlayerFromName(@Nullable String summonerName) {
    if (summonerName == null) return null;
    return new Query<>(Player.class).where("lol_name", summonerName).entity();
  }
}
