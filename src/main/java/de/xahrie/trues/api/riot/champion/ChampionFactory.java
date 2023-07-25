package de.xahrie.trues.api.riot.champion;

import de.xahrie.trues.api.riot.Xayah;
import org.jetbrains.annotations.NotNull;

public class ChampionFactory {
  public static void loadAllChampions() {
    Xayah.getChampions().forEach(ChampionFactory::getChampion);
  }

  public static Champion getChampion(@NotNull com.merakianalytics.orianna.types.core.staticdata.Champion riotChampion) {
    return new Champion(riotChampion.getId(), riotChampion.getName()).create();
  }

  public static Champion getChampion(String name) {
    return getChampion(Xayah.championNamed(name).get());
  }
}
