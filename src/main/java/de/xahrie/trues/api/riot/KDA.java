package de.xahrie.trues.api.riot;

import java.util.Set;

import no.stelar7.api.r4j.pojo.lol.match.v5.MatchParticipant;

public record KDA(short kills, short deaths, short assists) {
  public static KDA fromParticipant(MatchParticipant participant) {
    return new KDA((short) participant.getKills(), (short) participant.getDeaths(), (short) participant.getAssists());
  }

  public static KDA sum(Set<KDA> kdas) {
    final int kills = kdas.stream().mapToInt(KDA::kills).sum();
    final int deaths = kdas.stream().mapToInt(KDA::deaths).sum();
    final int assists = kdas.stream().mapToInt(KDA::assists).sum();
    return new KDA((short) kills, (short) deaths, (short) assists);
  }

  @Override
  public String toString() {
    return kills + "/" + deaths + "/" + assists;
  }
}
