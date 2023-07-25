package de.xahrie.trues.api.coverage.participator;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import de.xahrie.trues.api.coverage.match.log.MatchLog;
import de.xahrie.trues.api.coverage.participator.model.Lineup;
import de.xahrie.trues.api.coverage.participator.model.Participator;
import de.xahrie.trues.api.coverage.player.model.LoaderGameType;
import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.coverage.player.PlayerFactory;
import de.xahrie.trues.api.riot.performance.Lane;
import de.xahrie.trues.api.util.io.log.DevInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public abstract class AbstractTeamLineup {
  @Getter(AccessLevel.NONE)
  protected final Participator participator;
  protected List<Lineup> storedLineups;
  @Getter(AccessLevel.NONE)
  protected TeamLineup expectedLineups;

  public AbstractTeamLineup(Participator participator) {
    this.participator = participator;
    updateLineups();
  }

  @Nullable
  public Lineup getStoredLineup(@NonNull Lane lane) {
    if (lane.equals(Lane.UNKNOWN)) {
      final RuntimeException exception = new IllegalArgumentException("UNKNOWN darf hier nicht verwendet werden.");
      new DevInfo().error(exception);
      throw exception;
    }

    final List<Lineup> fixedLineups = getFixedLineups();
    if (fixedLineups == null) return null;
    return getFixedLineups().stream().filter(lineup -> lineup.getLane().equals(lane)).findFirst().orElse(null);
  }

  @Nullable
  public Lineup getStoredLineup(@NonNull Player player) {
    return getFixedLineups().stream().filter(lineup -> lineup.getPlayer().equals(player)).findFirst().orElse(null);
  }

  public List<Lineup> getFixedLineups() {
    if (storedLineups == null) updateLineups();
    return storedLineups;
  }

  /**
   * fuege Lineup hinzu - Wenn lineup auf der genannten Lane bereits exisitiert ersetze Lane des alten
   * Spielers durch Unknown
   *
   * @return Lineup fuer Chaining
   */
  public Lineup add(@NonNull Lineup lineup) {
    if (!lineup.getLane().equals(Lane.UNKNOWN)) {
      storedLineups.stream().filter(lineup1 -> lineup1.getLane().equals(lineup.getLane())).forEach(lineup1 -> lineup1.setLane(Lane.UNKNOWN));
    }
    return lineup;
  }

  public void remove(Lineup lineup) {
    storedLineups.remove(lineup);
  }

  public boolean setOrderedLineup(@NotNull String opGgUrl) {
    return setOrderedLineup(opGgUrl, new ArrayList<>(Arrays.asList(null, null, null, null, null)));
  }

  public boolean setOrderedLineup(@NotNull String opGgUrl, @NotNull List<Player> players) {
    opGgUrl = URLDecoder.decode(opGgUrl, StandardCharsets.UTF_8);
    final String[] split = opGgUrl.replace("https://www.op.gg/multisearch/euw?summoners=", "").split(",");
    for (int i = 0; i < split.length; i++) {
      if (i > 4) break;

      final String summonerName = split[i].replace("+", " ");
      if (summonerName.isBlank()) continue;

      final Player player = PlayerFactory.getPlayerFromName(summonerName);
      if (player != null) players.set(i, player);
    }
    if (players.stream().anyMatch(Objects::isNull)) return false;
    setLineup(players, true);
    return true;
  }

  public void setLineup(MatchLog log, boolean ordered) {
    setLineup(log.determineLineup(), ordered);
  }

  public void setLineup(List<Player> newLineup, boolean ordered) {
    storedLineups.stream().filter(lineup -> !newLineup.contains(lineup.getPlayer())).forEach(Lineup::delete);

    for (int i = 0; i < newLineup.size(); i++) {
      final Player player = newLineup.get(i);
      if (player != null) {
        final Lane lane = ordered ? Lane.values()[i + 1] : Lane.UNKNOWN;
        new Lineup(participator, player, lane).create();
        player.loadGames(LoaderGameType.CLASH_PLUS);
      }
    }
    updateLineups();
    updateMMR();
  }

  protected abstract void updateLineups();
  protected abstract void updateMMR();
}
