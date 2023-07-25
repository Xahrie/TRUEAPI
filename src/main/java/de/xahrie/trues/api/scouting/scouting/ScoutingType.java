package de.xahrie.trues.api.scouting.scouting;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ScoutingType {
  CHAMPIONS("Champions von "),
  HISTORY("Games von "),
  LINEUP("Lineup von "),
  MATCHUPS("Matchups von "),
  OVERVIEW("Übersicht von "),
  PLAYER_HISTORY("Matchhistory von "),
  SCHEDULE("Schedule von ");
  private final String titleStart;

  public static ScoutingType fromKey(String key) {
    return Arrays.stream(ScoutingType.values())
        .filter(type -> type.getTitleStart().split(" ")[0].equals(key))
        .findFirst().orElse(null);
  }
}
