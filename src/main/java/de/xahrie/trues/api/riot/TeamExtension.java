package de.xahrie.trues.api.riot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.xahrie.trues.api.riot.performance.ParticipantUtils;
import no.stelar7.api.r4j.pojo.lol.match.v5.ChampionBan;
import no.stelar7.api.r4j.pojo.lol.match.v5.LOLMatch;
import no.stelar7.api.r4j.pojo.lol.match.v5.MatchParticipant;
import no.stelar7.api.r4j.pojo.lol.match.v5.MatchTeam;

public class TeamExtension {
  public static List<Integer> getBanned(MatchTeam team) {
    return team.getBans().stream().map(ChampionBan::getChampionId).toList();
  }
  public static List<Integer> getPicks(MatchTeam team, LOLMatch match) {
    final List<Integer> integers = team.getBans().stream().map(ChampionBan::getPickTurn).toList();
    final List<Integer> finalized = new ArrayList<>();
    for (Integer integer : integers) {
      while (finalized.contains(integer)) integer++;
      finalized.add(integer);
    }

    final List<Integer> champions = ParticipantUtils.getParticipants(match, team.getTeamId())
                                                    .stream().map(MatchParticipant::getChampionId).toList();

    return finalized.stream().sorted().map(finalized::indexOf).map(champions::get).collect(Collectors.toList());
  }
}
