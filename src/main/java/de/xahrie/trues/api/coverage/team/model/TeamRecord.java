package de.xahrie.trues.api.coverage.team.model;

import de.xahrie.trues.api.util.Format;

public record TeamRecord(short seasons, short wins, short losses) {
  public Standing getStanding() {
    return new Standing(wins, losses);
  }

  @Override
  public String toString() {
    return getStanding().format(Format.SHORT) + " - " + seasons + " Seasons";
  }
}
