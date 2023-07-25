package de.xahrie.trues.api.scouting.scouting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.xahrie.trues.api.coverage.participator.TeamLineup;
import de.xahrie.trues.api.coverage.participator.model.Lineup;
import de.xahrie.trues.api.coverage.participator.model.Participator;
import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import de.xahrie.trues.api.datatypes.calendar.TimeFormat;
import de.xahrie.trues.api.discord.builder.embed.EmbedFieldBuilder;
import de.xahrie.trues.api.riot.performance.Lane;
import de.xahrie.trues.api.scouting.PlayerAnalyzer;
import de.xahrie.trues.api.scouting.ScoutingGameType;
import de.xahrie.trues.api.scouting.TeamAnalyzer;
import de.xahrie.trues.api.util.Util;
import net.dv8tion.jda.api.entities.MessageEmbed;

public record ScoutingEmbedHandler(AbstractTeam team, ScoutingGameType gameType, int days, int page, List<Lineup> lineups, Map<Player,PlayerAnalyzer> analyzerMap) {

  public ScoutingEmbedHandler(AbstractTeam team, List<Lineup> lineups, ScoutingGameType gameType, int days, int page) {
    this(team, gameType, days, page, lineups, new HashMap<>());
  }

  public List<MessageEmbed.Field> get(ScoutingType type, Participator participator) {
    return switch (type) {
      case CHAMPIONS -> getChampions();
      case HISTORY -> getHistory();
      case LINEUP -> getLineup(participator);
      case MATCHUPS -> getMatchups(participator);
      case OVERVIEW -> getOverview(participator);
      case PLAYER_HISTORY -> List.of();  // not handled here
      case SCHEDULE -> getSchedule();
    };
  }

  public List<MessageEmbed.Field> getOverview(Participator participator) {
    final List<MessageEmbed.Field> fields = new ArrayList<>();
    List<Lineup> l = lineups;
    if (participator == null) {
      participator = new Participator(null, true, team);
      final TeamLineup lineupCreator = participator.getTeamLineup(gameType, days);
      l = lineupCreator.getLineup();
    }
    for (Lineup lineup : l) {
      if (lineup.getPlayer() == null) fields.add(new MessageEmbed.Field("kein Spieler gefunden", "no Data", false));
      else {
        final PlayerAnalyzer playerAnalyzer = analyzerMap.computeIfAbsent(lineup.getPlayer(), player -> player.analyze(gameType, days));
        fields.addAll(playerAnalyzer.analyzePicks(lineup.getLane()));
      }
    }
    return fields;
  }

  public List<MessageEmbed.Field> getMatchups(Participator participator) {
    final List<MessageEmbed.Field> fields = new ArrayList<>();

    if (participator == null) participator = new Participator(null, true, team);
    final TeamLineup lineupCreator = participator.getTeamLineup(gameType, days);
    for (Lineup lineup : lineupCreator.getLineup()) {
      if (lineup.getPlayer() == null) fields.add(new MessageEmbed.Field("kein Spieler gefunden", "no Data", false));
      else {
        final PlayerAnalyzer playerAnalyzer = analyzerMap.computeIfAbsent(lineup.getPlayer(), player -> player.analyze(gameType, days));
        fields.addAll(playerAnalyzer.analyzeMatchups(lineup.getLane()));
      }
    }
    return fields;
  }

  public List<MessageEmbed.Field> getHistory() {
    return team.analyze(gameType, days).analyzeHistory(page);
  }

  public List<MessageEmbed.Field> getChampions() {
    final TeamAnalyzer analyze = team.analyze(gameType, days);
    return new EmbedFieldBuilder<>(analyze.handleChampions().stream().filter(championData -> championData.champion() != null).toList())
        .num("Champion", championData -> championData.champion().getName())
        .add("Picks", TeamAnalyzer.ChampionData::getPicksString)
        .add("KDA", TeamAnalyzer.ChampionData::getKDAString)
        .build();
  }

  public List<MessageEmbed.Field> getSchedule() {
    final TeamAnalyzer analyze = team.analyze(gameType, days);
    return new EmbedFieldBuilder<>(analyze.getMatches())
        .add("Spielzeit", match -> TimeFormat.DISCORD.of(match.getStart()))
        .add("Gegner", match -> Util.avoidNull(match.getOpponentOf(team), "keine Daten", AbstractTeam::getName))
        .add("Ergebnis", match -> match.getResult().toString()).build();
  }

  public List<MessageEmbed.Field> getLineup(Participator participator) {
    List<MessageEmbed.Field> fields = new ArrayList<>();
    if (participator == null) participator = new Participator(null, true, team);
    final TeamLineup lineupCreator = participator.getTeamLineup(gameType, days);
    for (Lane lane : Lane.values()) {
      if (lane.equals(Lane.UNKNOWN)) continue;
      fields = new EmbedFieldBuilder<>(fields, lineupCreator.getPlayersOnLane(lane))
          .add(lane.getDisplayName(), laneGames -> laneGames.player().getSummonerName())
          .add("Rank (Solo/Duo)", laneGames -> laneGames.player().getRanks().getCurrent().toString())
          .add("Lanegames", laneGames -> String.valueOf(laneGames.amount()))
          .build();
    }
    return fields;
  }
}
