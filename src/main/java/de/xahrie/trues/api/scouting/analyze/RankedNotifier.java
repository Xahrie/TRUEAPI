package de.xahrie.trues.api.scouting.analyze;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

  void handleNotifier(@NotNull Rank oldRank, @NotNull PlayerRank playerRank) {
    final DiscordUser user = player.getDiscordUser();
    final Rank newRank = playerRank.getRank();
    int diff = newRank.getMMR() - oldRank.getMMR();
    if (user == null) return;
    if (!user.isNotifyRank()) return;

    final Standing winrate = playerRank.getWinrate();
    final String description;
    if (winrate.getGames() < 10) {
      description = "Placements: " + winrate.format(Format.ADDITIONAL) + " (noch " + (10 - winrate.getGames()) + " Games)";
    } else if (winrate.getGames() == 10) {
      final String message = user.getMention() + " hat die Placements mit **" + winrate + "** beendet.";
      Jinx.instance.getChannels().getTextChannel(DefinedTextChannel.RANKED)
          .sendMessage(message).queue();
      description = "Placements beendet: " + winrate.format(Format.ADDITIONAL);
    } else {
      description = "SoloQueue: " + newRank + " - **" +
          playerRank.getWins() + "** Siege - **" + playerRank.getLosses() +
          "** Niederlagen (" + playerRank.getWinrate() .getWinrate() + ")";
    }

    if (handleMatch(user, diff, description)) return;

    handleRankup(oldRank, playerRank, user);
    sendRankupInfo();
  }

  private void handleRankup(Rank oldRank, @NotNull PlayerRank playerRank, DiscordUser user) {
    final Rank newRank = playerRank.getRank();

    if (!newRank.like(oldRank)) {
      if (Const.RANKED_STATE.equals(RankedState.DAILY)) {
        oldRanks.putIfAbsent(player.getId(), oldRank);
        return;
      }

      if (Const.RANKED_STATE.ordinal() < 2) {
        if (newRank.tier().equals(Rank.RankTier.UNRANKED)) return;

        final String message = user.getMention() + " (" + player.getSummonerName() +
            ") hat einen neuen Rank erreicht\n" + oldRank + " --> " + newRank;
        Jinx.instance.getChannels().getTextChannel(DefinedTextChannel.RANKED)
            .sendMessage(message).queue();
      }
    }
  }

  private void sendRankupInfo() {
    if (Duration.between(lastReset, LocalDateTime.now()).getSeconds() < 100 * 60) return;

    final int hour = LocalTime.now().getHour();
    if (hour != 9 && hour != 21) return;

    List<Player> playerList = SortedList.of(oldRanks.keySet().stream()
        .map(pId -> new Query<>(Player.class).entity(pId))
        .filter(player -> !player.getRanks().getCurrent().getRank().like(oldRanks.get(player.getId()))));

    if (!playerList.isEmpty()) {
      final EmbedBuilder builder = new EmbedBuilder().setTitle("Rankups")
          .setDescription("Rankups letzte 12 Stunden");
      new EmbedFieldBuilder<>(playerList)
          .add("Nutzer", player -> player.getDiscordUser().getMention())
          .add("Alter Rang", player -> String.valueOf(oldRanks.getOrDefault(player.getId(), new Rank(Rank.RankTier.UNRANKED,
              Rank.Division.I, (short) 0))))
          .add("Neuer Rang", player -> String.valueOf(player.getRanks().getCurrent().getRank()))
          .build().forEach(builder::addField);
      Jinx.instance.getChannels().getTextChannel(DefinedTextChannel.RANKED)
          .sendMessageEmbeds(builder.build()).queue();
    }

    oldRanks.clear();
    lastReset = LocalDateTime.now();
  }

  private boolean handleMatch(@NotNull DiscordUser user, int diff, @NotNull String description) {
    if (!Const.RANKED_STATE.equals(RankedState.MATCHES)) return false;
    if (playedPerformances.isEmpty()) return true;

    final EmbedBuilder embedBuilder = new EmbedBuilder()
        .setTitle("Neue Matches von " + user.getMention() + " (" + player.getSummonerName() + ")")
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
