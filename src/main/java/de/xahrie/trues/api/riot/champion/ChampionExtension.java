package de.xahrie.trues.api.riot.champion;

public class ChampionExtension {
  public static Champion getChampion(com.merakianalytics.orianna.types.core.staticdata.Champion champion) {
    return ChampionFactory.getChampion(champion);
  }
}
