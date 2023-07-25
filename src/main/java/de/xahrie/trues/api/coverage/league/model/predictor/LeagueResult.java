package de.xahrie.trues.api.coverage.league.model.predictor;

public record LeagueResult(int place, int points, Type type) {
  public static Type getType(int place, int with) {
    if (place <= 2) return place + with - 1 > 2 ? Type.MIGHT_ADVANCE : Type.ADVANCE;
    else if (place <= 6) return place + with - 1 > 6 ? Type.MIGHT_REGRESS : Type.HOLD;
    return Type.REGRESS;
  }

  public enum Type {
    ADVANCE,
    MIGHT_ADVANCE,
    HOLD,
    MIGHT_REGRESS,
    REGRESS
  }
}
