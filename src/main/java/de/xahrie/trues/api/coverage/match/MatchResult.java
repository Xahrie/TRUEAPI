package de.xahrie.trues.api.coverage.match;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import de.xahrie.trues.api.coverage.match.log.MatchLogAction;
import de.xahrie.trues.api.coverage.match.model.Match;
import de.xahrie.trues.api.coverage.participator.model.Participator;
import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import de.xahrie.trues.api.util.Const;
import de.xahrie.trues.api.util.StringUtils;
import de.xahrie.trues.api.util.Util;
import de.xahrie.trues.api.util.io.log.Console;
import de.xahrie.trues.api.util.io.log.DevInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
@ExtensionMethod(StringUtils.class)
public final class MatchResult implements Comparable<MatchResult> {
  public static MatchResult fromResultString(String resultString, Match match) {
    if (resultString.strip().matches("\\d+:\\d+"))
      return new MatchResult(match, resultString.strip().before(":").intValue(), resultString.after(":").intValue(), true);

    if (!resultString.equals("-:-")) {
      new DevInfo(resultString).with(Console.class).error(new IllegalArgumentException("Das Ergebnis ist nicht gültig!"));
      return null;
    }

    final long homeWins = match.getLogs(MatchLogAction.REPORT).stream().filter(log -> log.getParticipator().isHome()).count();
    final long guestWins = match.getLogs(MatchLogAction.REPORT).stream().filter(log -> !log.getParticipator().isHome()).count();
    return new MatchResult(match, (int) homeWins, (int) guestWins, false);
  }

  private final Match match;
  private final int homeScore;
  private final int guestScore;
  private final Boolean played;

  public int getMaxGames() {
    return match.getFormat().ordinal();
  }

  public MatchResult(Match match, int homeScore, int guestScore) {
    this(match, homeScore, guestScore, null);
  }

  public MatchResult add(boolean home) {
    return add(new MatchResult(match, home ? 1 : 0, home ? 0 : 1));
  }

  public Boolean getPlayed() {
    return played != null ? played : (homeScore + guestScore == getMaxGames());
  }

  public MatchResult add(MatchResult matchResult) {
    final int maxGames1 = matchResult.getMaxGames();
    final int maxGames = Math.max(maxGames1, getMaxGames());
    final int newHome = homeScore + matchResult.homeScore;
    final int newGuest = guestScore + matchResult.guestScore;
    Boolean newPlayed = newHome + newGuest >= maxGames;
    if (!newPlayed) newPlayed = played;
    return new MatchResult(match, newHome, newGuest, newPlayed);
  }

  public Boolean wasAcurate() {
    return played ? match.getResult().equals(determineExpectedResult()) : null;
  }

  public MatchResult expectResult() {
    return played ? match.getResult() : determineExpectedResult();
  }

  public MatchResult ofTeam(@NonNull AbstractTeam team) {
    if (match.getParticipator(team) == null) return null;
    if (team.equals(match.getHome().getTeam())) return this;
    return new MatchResult(match, guestScore, homeScore, played);
  }

  public List<MatchResult> getPossibleOutcomes() {
    final List<MatchResult> results = new ArrayList<>();
    if (getMaxGames() % 2 == 0) {
      if (homeScore + guestScore >= getMaxGames()) return List.of(this);
      for (int i = homeScore; i <= getMaxGames() - homeScore; i++)
        for (int j = guestScore; j <= getMaxGames() - guestScore; j++)
          if (i + j == getMaxGames()) results.add(new MatchResult(match, i, j));
      return results;
    }

    final int max = (int) Math.ceil(getMaxGames() / 2.);
    if (Math.max(homeScore, guestScore) >= max) return List.of(this);
    for (int i = homeScore; i <= max; i++)
      for (int j = guestScore; j <= max; j++)
        if (Math.max(i, j) < max) results.add(new MatchResult(match, i, j));
    return results;
  }

  public MatchResult determineExpectedResult() {
    final double gamePercentage = determineGamePercentage();
    if (gamePercentage == -1) return new MatchResult(match, 0, 0, true);

    if (getMaxGames() % 2 == 0) {
      final int remaining = getMaxGames() - homeScore - guestScore;
      final int score1 = (int) Math.round(gamePercentage * remaining);
      final int score2 = remaining - score1;
      return add(new MatchResult(match, score1, score2));
    }

    final int endAt = (int) Math.ceil(getMaxGames() / 2.);
    double score1 = homeScore;
    double score2 = guestScore;
    while (Math.round(Math.max(score1, score2)) < endAt) {
      score1 += gamePercentage;
      score2 += 1 - gamePercentage;
    }
    return new MatchResult(match, (int) Math.round(score1), (int) Math.round(score2), played);
  }

  public String getWinPercent() {
    final double gamePercentage = determineGamePercentage();
    return Math.round(1000 * (1 - gamePercentage) / 10) + "%";
  }

  /**
   * @return Wert zwischen 0 und 1 <p> Wie hoch die Chance, dass home ein Game gewinnt
   */
  private double determineGamePercentage() {
    final Integer homeMMR = Util.avoidNull(match.getHome(), 0, Participator::getMmr);
    final Integer guestMMR = Util.avoidNull(match.getGuest(), 0, Participator::getMmr);

    final double percentage = (homeMMR + guestMMR == 0) ? 0 : homeMMR * 1. / (guestMMR + homeMMR);
    if (percentage < .5 - Const.PREDICTION_FACTOR) return percentage / (2 - Const.PREDICTION_FACTOR * 4);
    if (percentage > .5 + Const.PREDICTION_FACTOR) return 1 - (1 - percentage) / (2 - Const.PREDICTION_FACTOR * 4);
    return (percentage - (.5 - Const.PREDICTION_FACTOR)) / (Const.PREDICTION_FACTOR * 4) + .25;
  }

  /**
   * @return Wie es angezeigt werden soll
   */
  @Override
  public String toString() {
    return getPlayed() ? homeScore + ":" + guestScore : "-:-";
  }

  public String currentResultString() {
    final String s = homeScore + ":" + guestScore;
    return getPlayed() ? s : "(" + s + ")";
  }

  @Override
  public int compareTo(@NotNull MatchResult o) {
    return Comparator.comparing(MatchResult::getHomeScore).compare(o, this);
  }
}
