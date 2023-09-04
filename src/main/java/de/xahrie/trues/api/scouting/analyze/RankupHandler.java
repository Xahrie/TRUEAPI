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
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.collections.SortedList;
import de.xahrie.trues.api.discord.builder.embed.EmbedFieldBuilder;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.discord.util.DefinedTextChannel;
import de.xahrie.trues.api.discord.util.Jinx;
import de.xahrie.trues.api.riot.RankedState;
import de.xahrie.trues.api.util.Const;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

public class RankupHandler {
  private static final Map<Integer, Rank> oldRanks = Collections.synchronizedMap(new HashMap<>());
  private static LocalDateTime lastReset = LocalDateTime.now();

  static void handleRankup(Rank oldRank, @NotNull PlayerRank playerRank, DiscordUser user) {
    final Rank newRank = playerRank.getRank();
    final Player player = playerRank.getPlayer();

    if (!(newRank.like(oldRank))) {
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

  static void sendRankupInfo() {
    if (Duration.between(lastReset, LocalDateTime.now()).getSeconds() < 100*60) return;

    final int hour = LocalTime.now().getHour();
    if (hour != 9 && hour != 21) return;

    List<Player> playerList = SortedList.of();
    for (final Map.Entry<Integer, Rank> entry : oldRanks.entrySet()) {
      final Player player = new Query<>(Player.class).entity(entry.getKey());
      final Rank currentRank = player.getRanks().getCurrent().getRank();
      final Rank previousRank = entry.getValue();
      if (currentRank.like(previousRank)) playerList.add(player);
    }

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
}
