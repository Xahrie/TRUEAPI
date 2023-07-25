package de.xahrie.trues.api.coverage.team.model;

import java.io.Serial;
import java.io.Serializable;

import de.xahrie.trues.api.datatypes.number.TrueNumber;
import de.xahrie.trues.api.util.Format;
import org.jetbrains.annotations.NotNull;

public record Standing(int wins, int losses) implements Serializable, Comparable<Standing> {
  @Serial
  private static final long serialVersionUID = -8830455150033695496L;

  public Winrate getWinrate() {
    return new Winrate(new TrueNumber((double) this.wins).divide(this.wins + this.losses));
  }

  public int getGames() {
    return wins + losses;
  }

  @Override
  public String toString() {
    return format(Format.LONG);
  }

  public String format(@NotNull Format format) {
    return switch (format) {
      case ADDITIONAL -> wins + ":" + losses + " (" + getWinrate() + ")";
      case SHORT -> wins + ":" + losses;
      case LONG -> wins + " : " + losses;
      case MEDIUM -> wins + " - " + getWinrate();
    };
  }

  @Override
  public int compareTo(@NotNull Standing o) {
    return getWinrate().compareTo(o.getWinrate());
  }

  public record Winrate(TrueNumber rate) implements Comparable<Winrate> {
    @Override
    public String toString() {
      return rate.percentValue();
    }

    @Override
    public int compareTo(@NotNull Winrate o) {
      return rate.compareTo(o.rate);
    }
  }
}
