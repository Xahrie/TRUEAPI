package de.xahrie.trues.api.coverage.team.model;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

import de.xahrie.trues.api.util.Format;
import de.xahrie.trues.api.util.StringUtils;
import de.xahrie.trues.api.util.Util;
import de.xahrie.trues.api.util.io.request.URLType;
import org.jetbrains.annotations.NotNull;

public record TeamScore(Short place, Short wins, Short losses) implements Serializable, Comparable<TeamScore> {
  public static TeamScore disqualified() {
    return new TeamScore(null, null, null);
  }

  public static TeamScore of(String input) {
    if (input.equals("Disqualifiziert")) return TeamScore.disqualified();

    String place = input.split("\\.")[0];
    if (place.contains(":")) place = StringUtils.after(place, ":");
    final short placeInteger = Short.parseShort(place.strip());
    final String wins = input.split("\\(")[1].split("/")[0];
    final short winsInteger = Short.parseShort(wins.strip());
    final String losses = input.split("/")[1].split("\\)")[0];
    final short lossesInteger = Short.parseShort(losses.strip());
    return new TeamScore(placeInteger, winsInteger, lossesInteger);
  }

  public Standing getStanding() {
    int wins = wins() == null ? 0 : (int) wins();
    int losses = losses() == null ? 0 : (int) losses();
    return new Standing(wins, losses);
  }

  TeamDestination getDestination() {
    if (this.place < 3) return TeamDestination.PROMOTION;
    if (this.place > 6) return TeamDestination.DEMOTION;
    return TeamDestination.STAY;
  }

  @Override
  public String toString() {
    return this.place + ". " + getStanding().format(Format.ADDITIONAL);
  }

  @Override
  public int compareTo(@NotNull TeamScore o) {
    return Comparator.comparing(TeamScore::place, Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing((TeamScore o1) -> o1.getStanding().getWinrate().rate(), Comparator.reverseOrder())
        .compare(this, o);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof final TeamScore teamScore)) return false;
    return Objects.equals(place, teamScore.place) && Objects.equals(wins, teamScore.wins) && Objects.equals(losses, teamScore.losses);
  }

  @Override
  public int hashCode() {
    return Objects.hash(place, wins, losses);
  }

  public boolean isDisqualified() {
    return place == null && wins == null && losses == null;
  }
}
