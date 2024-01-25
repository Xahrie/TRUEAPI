package de.xahrie.trues.api.coverage.league.model;

import java.util.Arrays;

import de.xahrie.trues.api.util.Const;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum LeagueTier {
  Division_1(1),
  Division_1_Playoffs(null),
  Division_2(2),
  Division_2_Playoffs(null),
  Division_3_Playoffs(null),
  Division_3(3),
  Division_4_Playoffs(null),
  Division_4(4),
  Division_5_Playoffs(null),
  Division_5(5),
  Division_6_Playoffs(null),
  Division_6(6),
  Division_7_Playoffs(null),
  Division_7(7),
  Division_8_Playoffs(null),
  Division_8(8),
  Division_9(9),
  Swiss_Starter(10);

  private static int idFromName(String name) {
    if (name.startsWith("Gruppe ")) {
      return Integer.parseInt(name.replace("Gruppe ", "").split("\\.")[0]);
    }
    return name.equals(Const.Gamesports.STARTER_NAME) ? 10 : -1;
  }

  public static LeagueTier fromName(String name) {
    return fromIndex(idFromName(name));
  }

  public static LeagueTier fromIndex(int index) {
    if (index > 8) return Division_8;
    if (index < 3) return Division_3_Playoffs;
    return Arrays.stream(LeagueTier.values()).filter(tier -> tier.index != null && tier.index == index).findFirst().orElse(null);
  }

  private final Integer index;
}
