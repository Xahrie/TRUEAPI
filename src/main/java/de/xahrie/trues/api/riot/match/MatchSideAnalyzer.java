package de.xahrie.trues.api.riot.match;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.xahrie.trues.api.coverage.player.PlayerFactory;
import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.riot.performance.Lane;
import de.xahrie.trues.api.riot.performance.Matchup;
import de.xahrie.trues.api.riot.performance.ParticipantUtils;
import de.xahrie.trues.api.riot.performance.Performance;
import de.xahrie.trues.api.riot.performance.TeamPerf;
import de.xahrie.trues.api.riot.KDA;
import de.xahrie.trues.api.riot.TeamExtension;
import de.xahrie.trues.api.riot.champion.Champion;
import de.xahrie.trues.api.riot.game.Game;
import de.xahrie.trues.api.riot.game.GameType;
import de.xahrie.trues.api.riot.game.MatchUtils;
import de.xahrie.trues.api.riot.game.Selection;
import de.xahrie.trues.api.util.Util;
import lombok.Data;
import lombok.experimental.ExtensionMethod;
import no.stelar7.api.r4j.pojo.lol.match.v5.LOLMatch;
import no.stelar7.api.r4j.pojo.lol.match.v5.MatchParticipant;
import no.stelar7.api.r4j.pojo.lol.match.v5.MatchTeam;
import no.stelar7.api.r4j.pojo.lol.match.v5.ObjectiveStats;

@Data
@ExtensionMethod({TeamExtension.class, ParticipantUtils.class})
public class MatchSideAnalyzer {
  private final Player originatedPlayer;
  private final LOLMatch match;
  private final Game game;
  private final Side side;
  private final MatchTeam team;
  private final List<MatchParticipant> validParticipants;

  public MatchSideAnalyzer(Player originatedPlayer, LOLMatch match, Game game, Side side) {
    this.originatedPlayer = originatedPlayer;
    this.match = match;
    this.game = game;
    this.side = side;
    this.team = side.getTeam(match);
    this.validParticipants = new TeamParticipantsAnalyzer(originatedPlayer, match, team).analyze();
  }

  public TeamPerf analyze() {
    final List<MatchParticipant> participants = ParticipantUtils.getParticipants(match, team.getTeamId());
    final KDA kda = KDA.sum(participants.stream().map(KDA::fromParticipant).collect(Collectors.toSet()));
    final int gold = participants.stream().mapToInt(MatchParticipant::getGoldEarned).sum();
    final int damage = participants.stream().mapToInt(MatchParticipant::getTotalDamageDealtToChampions).sum();
    final int vision = participants.stream().mapToInt(MatchParticipant::getVisionScore).sum();
    final int creeps = participants.stream().mapToInt(MatchParticipant::getTotalMinionsKilled).sum();
    final Map<String, ObjectiveStats> obj = team.getObjectives();
    final TeamPerf.Objectives objectives = new TeamPerf.Objectives(obj.get("tower").getKills(), obj.get("dragon").getKills(),
        obj.get("inhibitor").getKills(), obj.get("riftHerald").getKills(), obj.get("baron").getKills());
    final var teamPerformance = new TeamPerf(game, side, team.didWin(), kda, gold, damage, vision, creeps, objectives).create();
    handleTeamOfTeamPerf(teamPerformance);
    validParticipants.forEach(participant -> new ParticipantsAnalyzer(match, teamPerformance, team, participant).analyze());
    return teamPerformance;
  }

  private void handleTeamOfTeamPerf(TeamPerf teamPerformance) {
    if (teamPerformance.getTeam() == null) {
      final List<Player> players = ParticipantUtils.getParticipants(match, team.getTeamId()).stream()
                                                   .map(p -> PlayerFactory.findPlayer(p.getPuuid())).filter(Objects::nonNull).toList();
      final Map<Integer, Long> teams = players.stream()
          .map(Player::getTeamId)
          .filter(Objects::nonNull)
          .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
      if (teams.isEmpty()) return;
      final Map.Entry<Integer, Long> maxEntry = Collections.max(teams.entrySet(), Map.Entry.comparingByValue());
      if (maxEntry.getValue() > 2) {
        final Integer teamId = maxEntry.getKey();
        final AbstractTeam t = new Query<>(AbstractTeam.class).entity(teamId);
        if (t.getOrgaTeam() != null) {
          game.setOrgaGame(true);
          if (game.getType().equals(GameType.TOURNAMENT)) players.forEach(player -> player.setPlayed(true));
        }
        teamPerformance.setTeam(t);
      }
    }
  }

  public void analyzeSelections() {
    final MatchTeam team = side.getTeam(match);
    handleSelectionType(team.getBanned(), Selection.SelectionType.BAN);
    handleSelectionType(team.getPicks(match), Selection.SelectionType.PICK);
  }

  private void handleSelectionType(List<Integer> champs, Selection.SelectionType type) {
    for (int i = 0; i < champs.size(); i++) {
      Integer championId = champs.get(i);
      if (championId == null || championId == -1) championId = 0;
      new Selection(game, side, type, (byte) (i + 1), championId).create();
    }
  }

  public record TeamParticipantsAnalyzer(Player originatedPlayer, LOLMatch match, MatchTeam team) {
    public List<MatchParticipant> analyze() {
      final List<MatchParticipant> participants = ParticipantUtils.getParticipants(match, team.getTeamId());
      if (MatchUtils.getGameQueue(match).equals(GameType.TOURNAMENT) || MatchUtils.getGameQueue(match).equals(GameType.CUSTOM)) {
        return participants;
      }

      final List<Object> puuids = participants.stream().map(MatchParticipant::getPuuid).map(str -> (Object) str).toList();
      final List<String> validPuuids = new Query<>(Player.class, "SELECT lol_puuid FROM player JOIN team t on player.team = t.team_id WHERE refresh >= now() OR discord_user is not null AND lol_puuid IN (?, ?, ?, ?, ?)").list(puuids).stream()
          .map(array -> (String) array[0]).toList();
      List<MatchParticipant> players = participants.stream().filter(participant -> validPuuids.contains(participant.getPuuid())).toList();
      if (players.isEmpty()) {
        players = participants.stream().filter(participant -> participant.getPuuid().equals(originatedPlayer.getPuuid())).toList();
      }
      return MatchUtils.getGameQueue(match).equals(GameType.CLASH) && !players.isEmpty() ? participants : players;
    }
  }

  public record ParticipantsAnalyzer(LOLMatch match, TeamPerf teamPerformance, MatchTeam team, MatchParticipant participant) {
    public Performance analyze() {
      final Player player = PlayerFactory.getPlayerFromPuuid(participant.getPuuid());
      final Lane lane = ParticipantUtils.getPlayedLane(participant);
      final Champion selectedChampion = ParticipantUtils.getSelectedChampion(participant);
      final MatchParticipant opponent = ParticipantUtils.getOpponent(participant, match);
      final Champion opposingChampion = Util.avoidNull(opponent, ParticipantUtils::getSelectedChampion);
      final Matchup matchup = new Matchup(selectedChampion, opposingChampion);
      final KDA kda = KDA.fromParticipant(participant);
      return new Performance(teamPerformance, player, lane, matchup, kda, participant.getGoldEarned(), participant.getTotalDamageDealtToChampions(), participant.getVisionScore(), participant.getTotalMinionsKilled() + participant.getNeutralMinionsKilled()).create();
    }
  }
}
