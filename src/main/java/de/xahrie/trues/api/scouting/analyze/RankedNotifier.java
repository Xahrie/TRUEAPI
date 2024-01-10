package de.xahrie.trues.api.scouting.analyze;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.coverage.player.model.PlayerRank;
import de.xahrie.trues.api.coverage.player.model.Rank;
import de.xahrie.trues.api.coverage.team.model.Standing;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.TimeFormat;
import de.xahrie.trues.api.datatypes.collections.SortedList;
import de.xahrie.trues.api.discord.builder.embed.EmbedFieldBuilder;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.discord.util.DefinedTextChannel;
import de.xahrie.trues.api.discord.util.Jinx;
import de.xahrie.trues.api.riot.RankedState;
import de.xahrie.trues.api.riot.champion.Champion;
import de.xahrie.trues.api.riot.game.Game;
import de.xahrie.trues.api.riot.performance.Performance;
import de.xahrie.trues.api.util.Const;
import de.xahrie.trues.api.util.Format;
import de.xahrie.trues.api.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

public record RankedNotifier(Player player, List<Performance> playedPerformances) {
  private static final Map<Integer, Rank> oldRanks = Collections.synchronizedMap(new HashMap<>());
  private static LocalDateTime lastReset = LocalDateTime.now();

  private void resetRankNotifier() {
    oldRanks.clear();
    lastReset = LocalDateTime.now();
  }

  void handleNotifier(@NotNull Rank oldRank, @NotNull PlayerRank playerRank) {
    final DiscordUser user = player.getDiscordUser();
    if (user == null) return;
    if (!user.isNotifyRank()) return;
    if (user.getMember() == null) return;

    final Rank newRank = playerRank.getRank();
    int diff = newRank.getMMR() - oldRank.getMMR();
    final String description = determineDescription(playerRank, user, newRank);

    if (handleMatch(user, diff, description)) return; // noting played

    handleRankup(oldRank, playerRank, user);
    sendRankupInfo();
  }

  @NotNull
  private static String determineDescription(@NotNull PlayerRank playerRank, @NotNull DiscordUser user, Rank newRank) {
    final Standing winrate = playerRank.getWinrate();
    if (winrate.getGames() < Const.PLACEMENT_GAMES) {
      int gamesRemaining = Const.PLACEMENT_GAMES - winrate.getGames();
      return "Placements: " + winrate.format(Format.ADDITIONAL) + " (noch " + gamesRemaining + " Games)";
    }

    if (winrate.getGames() == Const.PLACEMENT_GAMES) {
      String message = user.getMention() + " hat die Placements mit **" + winrate + "** beendet.";
      Jinx.instance.getChannels().getTextChannel(DefinedTextChannel.RANKED).sendMessage(message).queue();
      return "Placements beendet: " + winrate.format(Format.ADDITIONAL);
    }

    return "SoloQueue: " + newRank + " - **" + playerRank.getWins() + "** Siege - **" +
        playerRank.getLosses() + "** Niederlagen (" + playerRank.getWinrate() .getWinrate() + ")";
  }

  private void handleRankup(Rank oldRank, @NotNull PlayerRank playerRank, DiscordUser user) {
    final Rank newRank = playerRank.getRank();

    if (newRank.like(oldRank)) return;

    if (Const.RANKED_STATE.equals(RankedState.DAILY)) {
      oldRanks.putIfAbsent(player.getId(), oldRank);
      return;
    }

    if (Const.RANKED_STATE.hasUserMessage()) { // Matches, Rankups
      if (newRank.tier().equals(Rank.RankTier.UNRANKED)) return;

      if (Const.RANKED_STATE.equals(RankedState.TIER_RANKUPS) && newRank.tier() == oldRank.tier())
        return;

      final String message = user.getMention() + " (" + player.getName() +
          ") hat einen neuen Rank erreicht\n" + oldRank + " --> " + newRank;
      Jinx.instance.getChannels().getTextChannel(DefinedTextChannel.RANKED).sendMessage(message).queue();
    }
  }

  private void sendRankupInfo() {
    if (Duration.between(lastReset, LocalDateTime.now()).getSeconds() < 100 * 60)
      return; // prevent sending info two times within the same hour

    int hour = LocalTime.now().getHour();
    if (!Const.RANK_UPDATE_HOURS.contains(hour)) return;

    List<Player> playerList = SortedList.of(oldRanks.keySet().stream()
        .map(pId -> new Query<>(Player.class).entity(pId))
            .filter(player -> player.getDiscordUser().isActive())
        .filter(player -> !player.getRanks().getCurrent().getRank().like(oldRanks.get(player.getId()))));
    List<PlayerRankChange> rankChanges = playerList.stream()
        .map(p -> new PlayerRankChange(p, oldRanks.get(p.getId()), p.getRanks().getCurrent().getRank())).toList();

    if (!playerList.isEmpty()) {
      int interval = (int) Math.round(24. / Const.RANK_UPDATE_HOURS.size());
      final EmbedBuilder builder = new EmbedBuilder().setTitle("Rank Updates (Solo/Duo)")
          .setDescription("Rankups letzte " + interval + " Stunden");
      new EmbedFieldBuilder<>(rankChanges.stream().filter(playerRankChange -> playerRankChange.isPromoted() == 1).toList())
          .add("Promotions", change -> change.player().getDiscordUser().getMention())
          .add("Alter Rang", change -> String.valueOf(change.oldRank()))
          .add("Neuer Rang", change -> String.valueOf(change.newRank()))
          .build().forEach(builder::addField);

      new EmbedFieldBuilder<>(rankChanges.stream().filter(playerRankChange -> playerRankChange.isPromoted() == -1).toList())
          .add("Demotions", change -> change.player().getDiscordUser().getMention())
          .add("Alter Rang", change -> String.valueOf(change.oldRank()))
          .add("Neuer Rang", change -> String.valueOf(change.newRank()))
          .build().forEach(builder::addField);
      Jinx.instance.getChannels().getTextChannel(DefinedTextChannel.RANKED)
          .sendMessageEmbeds(builder.build()).queue();
    }

    resetRankNotifier();
  }

  private boolean handleMatch(@NotNull DiscordUser user, int diff, @NotNull String description) {
    if (!Const.RANKED_STATE.equals(RankedState.MATCHES)) return false;
    if (playedPerformances.isEmpty()) return true;

    final EmbedBuilder embedBuilder = new EmbedBuilder()
        .setTitle("Neue Matches von " + user.getMention() + " (" + player.getName() + ")")
        .setDescription(description);
    for (int i = 0; i < playedPerformances.size(); i++) {
      if (i == 20) break;
      final Performance performance = playedPerformances.get(i);
      final Game game = performance.getTeamPerformance().getGame();
      final String enemyChamp = Util.avoidNull(performance.getMatchup().getOpposingChampion(), "null",
          Champion::getName);
      embedBuilder.addField(
          performance.getTeamPerformance().getWinString() + " mit " +
              performance.getMatchup().getChampion().getName() + " (" +
              performance.getKda().toString() + ") gegen " + enemyChamp + " (" +
              (diff >= 0 ? "+" : "") + diff + ")",
          TimeFormat.DISCORD.of(game.getEnd()) + " (" + game.getDuration() +
              ") - [Zum Match](https://www.leagueofgraphs.com/match/euw/" +
              game.getGameId().replace("EUW1_", "") + ")", false);
    }
    Jinx.instance.getChannels().getTextChannel(DefinedTextChannel.RANKED)
        .sendMessageEmbeds(embedBuilder.build()).queue();
    return false;
  }
}
