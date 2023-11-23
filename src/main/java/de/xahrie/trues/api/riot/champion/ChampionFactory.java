package de.xahrie.trues.api.riot.champion;

import com.merakianalytics.orianna.types.common.OriannaException;
import de.xahrie.trues.api.riot.Xayah;
import de.xahrie.trues.api.util.io.log.DevInfo;
import org.jetbrains.annotations.NotNull;

public class ChampionFactory {
  public static void loadAllChampions() {
    try {
      Xayah.getChampions().forEach(ChampionFactory::getChampion);
    } catch (OriannaException exception) {
      new DevInfo("Orianna API outdated").exception(exception);
    }

  }

  public static Champion getChampion(@NotNull com.merakianalytics.orianna.types.core.staticdata.Champion riotChampion) {
    return new Champion(riotChampion.getId(), riotChampion.getName(), riotChampion.getKey()).create();
  }

  public static Champion getChampion(String name) {
    return getChampion(Xayah.championNamed(name).get());
  }
}
