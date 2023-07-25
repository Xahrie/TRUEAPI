package de.xahrie.trues.api.community.betting;

import java.math.BigDecimal;

import de.xahrie.trues.api.coverage.match.model.Match;
import de.xahrie.trues.api.coverage.match.MatchResult;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.util.Util;
import lombok.NonNull;

public class BetFactory {
  public static double quote(MatchResult result) {
    if (!result.getPlayed()) return 0.;

    final Object[] outcome = new Query<>(Bet.class).get("COALESCE(sum(bet_amount),0) + 1", Integer.class).where("bet_outcome", result.toString()).and("coverage", result.getMatch()).single();
    final Object[] all = new Query<>(Bet.class).get("COALESCE(sum(bet_amount),0) + 1", Integer.class).where("coverage", result.getMatch()).single();
    return ((BigDecimal) all[0]).intValue() * 1. / ((BigDecimal) outcome[0]).intValue();
  }

  public static boolean bet(DiscordUser user, @NonNull Match match, String outcome, int amount) {
    final Bet currentBet = getBet(user, match);
    final int remainingPoints = user.getPoints() + Util.avoidNull(currentBet, 0, Bet::getAmount) - amount;
    if (remainingPoints < 0) return false;

    user.setPoints(remainingPoints);
    new Bet(match, user, outcome, amount).create();
    return true;
  }

  public static Bet getBet(DiscordUser user, Match match) {
    return new Query<>(Bet.class).where("discord_user", user).and("coverage", match).entity();
  }
}
