package de.xahrie.trues.api.scouting.analyze;

import de.xahrie.trues.api.coverage.player.PlayerHandler;
import de.xahrie.trues.api.coverage.player.model.LoaderGameType;
import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.coverage.player.model.PlayerRank;
import de.xahrie.trues.api.coverage.player.model.Rank;
import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import de.xahrie.trues.api.coverage.team.model.Standing;
import de.xahrie.trues.api.database.connector.Database;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.TimeFormat;
import de.xahrie.trues.api.datatypes.collections.SortedList;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.discord.util.DefinedTextChannel;
import de.xahrie.trues.api.discord.util.Jinx;
import de.xahrie.trues.api.riot.RankedState;
import de.xahrie.trues.api.riot.Zeri;
import de.xahrie.trues.api.riot.champion.Champion;
import de.xahrie.trues.api.riot.champion.ChampionMastery;
import de.xahrie.trues.api.riot.game.Game;
import de.xahrie.trues.api.riot.game.GameType;
import de.xahrie.trues.api.riot.match.MatchHistoryBuilder;
import de.xahrie.trues.api.riot.match.RiotMatchAnalyzer;
import de.xahrie.trues.api.riot.performance.Performance;
import de.xahrie.trues.api.scouting.AnalyzeManager;
import de.xahrie.trues.api.util.Const;
import de.xahrie.trues.api.util.Format;
import de.xahrie.trues.api.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard;
import no.stelar7.api.r4j.basic.constants.api.regions.RegionShard;
import no.stelar7.api.r4j.basic.constants.types.lol.GameQueueType;
import no.stelar7.api.r4j.basic.constants.types.lol.MapType;
import no.stelar7.api.r4j.pojo.lol.match.v5.LOLMatch;
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public record RiotPlayerAnalyzer(Player player, List<Performance> playedPerformances) {
  private static final List<Integer> todayAnalyzedPlayers =
          Collections.synchronizedList(SortedList.of());
  private static final List<Integer> fullyAnalyzedPlayers =
          Collections.synchronizedList(SortedList.of());
  private static final List<Integer> currentPlayers = Collections.synchronizedList(SortedList.of());

  public static void reset() {
    fullyAnalyzedPlayers.clear();
    todayAnalyzedPlayers.clear();
  }

  public RiotPlayerAnalyzer(Player player) {
    this(player, SortedList.of());
  }

  public void analyzeGames(LoaderGameType gameType, boolean force) {
    if (currentPlayers.contains(player.getId())) return;
    if (gameType.equals(LoaderGameType.MATCHMADE) &&
        fullyAnalyzedPlayers.contains(player.getId()) && !force) return;


    currentPlayers.add(player.getId());
    fullyAnalyzedPlayers.add(player.getId());

    final Summoner summoner =
            Zeri.get().getSummonerAPI().getSummonerByPUUID(LeagueShard.EUW1, player.getPuuid());
    player.setSummonerName(summoner.getName());
    final LocalDateTime currentTime = LocalDateTime.now();

    if (new Query<>(Performance.class,
                    "SELECT performance.* FROM performance JOIN team_perf tp on " +
                    "performance.t_perf = tp.team_perf_id JOIN game g on tp.game = g.game_id WHERE " +
                    "g.game_type <= ?").entity(List.of(GameType.CUSTOM)) == null) {
      final var historyBuilder = new MatchHistoryBuilder(summoner, LocalDateTime.MIN)
              .with(GameQueueType.CUSTOM);
      analyzeGames(historyBuilder.get(), gameType);
    }

    final var historyBuilder = gameType.getMatchHistory(summoner, player);
    if (analyzeGames(historyBuilder, gameType)) {
      final PlayerRank old = player.getRanks().getCurrent();
      final PlayerRank rank = new PlayerHandler(null, player).updateElo();
      handleNotifier(old.getRank(), rank);
    } else player.getRanks().createRank();

    AnalyzeManager.delete(player);

    if (gameType.equals(LoaderGameType.MATCHMADE)) player.setUpdated(currentTime);

    currentPlayers.remove(Integer.valueOf(player.getId()));
    Database.connection().commit(null);
  }

  private boolean analyzeGames(List<String> history, LoaderGameType gameType) {
    final long start = System.currentTimeMillis();
    boolean hasPlayedRanked = false;
    for (String matchId : new HashSet<>(history)) {
      final LOLMatch match = Zeri.get().getMatchAPI().getMatch(RegionShard.EUROPE, matchId);
      if (match.getParticipants().size() != 10) continue;
      if (!match.getMap().equals(MapType.SUMMONERS_RIFT)) continue;
      if (List.of(GameQueueType.BOT_5X5_INTRO, GameQueueType.BOT_5X5_BEGINNER,
                  GameQueueType.BOT_5X5_INTERMEDIATE, GameQueueType.ALL_RANDOM_URF,
                  GameQueueType.ULTBOOK).contains(match.getQueue())) continue;


      final RiotMatchAnalyzer matchAnalyzer = new RiotMatchAnalyzer(player, match);
      final Game game = matchAnalyzer.analyze();
      if (game != null && game.getType().equals(GameType.RANKED_SOLO)) {
        hasPlayedRanked = true;
        game.getTeamPerformances().stream().flatMap(teamPerf -> teamPerf.getPerformances().stream())
            .filter(performance -> performance.getPlayerId() == player.getId()).findFirst()
            .ifPresent(playedPerformances::add);
      }
    }

    if (gameType.equals(LoaderGameType.MATCHMADE) && history.size() > 20)
      System.out.println(
              player.getSummonerName() + " (" +
              Util.avoidNull(player.getTeam(), "null", AbstractTeam::getName) + ") -> " +
              (System.currentTimeMillis() - start) / 1000.0 + " für " + history.size()
      );


    if (!history.isEmpty() && !todayAnalyzedPlayers.contains(player.getId())) analyzeMastery();

    return hasPlayedRanked;
  }

  private void handleNotifier(@NotNull Rank oldRank, @NotNull PlayerRank playerRank) {
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

    if (Const.RANKED_STATE.equals(RankedState.MATCHES)) {
      if (playedPerformances.isEmpty()) return;

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
    }

    RankupHandler.handleRankup(oldRank, playerRank, user);
    RankupHandler.sendRankupInfo();
  }

  public void analyzeMastery() {
    todayAnalyzedPlayers.add(player.getId());
    final Summoner summonerByPUUID =
            Zeri.get().getSummonerAPI().getSummonerByPUUID(LeagueShard.EUW1, player.getPuuid());
    final String summonerId = summonerByPUUID.getSummonerId();
    for (no.stelar7.api.r4j.pojo.lol.championmastery.ChampionMastery championMastery :
            Zeri.get().getMasteryAPI().getChampionMasteries(LeagueShard.EUW1, summonerId)) {
      new ChampionMastery(player, championMastery.getChampionId(),
                          championMastery.getChampionPoints(),
                          (byte) championMastery.getChampionLevel(),
                          championMastery.getLastPlayTimeAsDate().toLocalDateTime()).create();
    }
  }
}
