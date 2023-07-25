package de.xahrie.trues.api.riot.performance;

import java.util.List;

import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.riot.champion.Champion;
import de.xahrie.trues.api.riot.match.Side;
import no.stelar7.api.r4j.basic.constants.types.lol.TeamType;
import no.stelar7.api.r4j.pojo.lol.match.v5.LOLMatch;
import no.stelar7.api.r4j.pojo.lol.match.v5.MatchParticipant;
import no.stelar7.api.r4j.pojo.lol.match.v5.MatchTeam;

public class ParticipantUtils {
  public static List<MatchParticipant> getParticipants(LOLMatch match, TeamType side) {
    return match.getParticipants().stream().filter(matchParticipant -> matchParticipant.getTeam().equals(side)).toList();
  }

  public static Lane getPlayedLane(MatchParticipant participant) {
    return Lane.transform(participant.getChampionSelectLane());
  }

  public static Champion getSelectedChampion(MatchParticipant participant) {
    return new Query<>(Champion.class).entity(participant.getChampionId());
  }

  public static MatchParticipant getOpponent(MatchParticipant participant, LOLMatch match) {
    final Lane playedLane = getPlayedLane(participant);
    final Side side = Side.valueOf(participant.getTeam().name());

    final MatchTeam opposingTeam = side.getOpponent(match);
    if (opposingTeam == null) return null;

    final List<MatchParticipant> participants = ParticipantUtils.getParticipants(match, opposingTeam.getTeamId());
    return participants.stream().filter(part -> getPlayedLane(part).equals(playedLane)).findFirst().orElse(null);
  }
}
