package de.xahrie.trues.api.util.io.request;

import de.xahrie.trues.api.util.io.cfg.JSON;

/**
 * Created by Lara on 14.02.2023 for TRUEbot
 */
public enum URLType {
  LEAGUE,
  PLAYOFF,
  MATCH,
  PLAYER,
  TEAM;

  public String getUrlName() {
    final var apiConfig = JSON.read("apis.json");
    final var primeConfig = apiConfig.getJSONObject("gamesports");
    final var primeEndpoints = primeConfig.getJSONObject("endpoints");
    return primeEndpoints.getString(this.name().toLowerCase());
  }

}
