package de.xahrie.trues.api.discord.notify;

import java.time.LocalDateTime;
import java.time.LocalTime;

import de.xahrie.trues.api.coverage.ABetable;
import de.xahrie.trues.api.coverage.match.model.Match;
import de.xahrie.trues.api.database.query.Condition;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.TimeFormat;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.util.Util;

public class BetNotifier extends Notifier {
  private final ABetable betable;

  public BetNotifier(LocalTime localTime, ABetable betable) {
    super(localTime, null);
    this.betable = betable;
  }

  @Override
  public void sendNotification() {
    if (betable instanceof Match match) {
      final int offset = Math.abs(Util.avoidNull((int) match.getRateOffset(), 0));
      final LocalDateTime start = match.getStart().minusMinutes(offset);
      final String message = "Die Tippannahme für das Match **" + match.getId() + ": " + match.getMatchup() +
          "** endet " + TimeFormat.DISCORD.of(start) +
          ".\nDu kannst deine Wette mit <ID>: <Ergebnis mit Minuszeichen getrennt> abgeben. Beispiel: `123: 3-2`" +
          "\n\n_Wenn du diese Nachricht nicht mehr sehen möchtest nutze auf dem Discord-Server den Command " +
          "/settings tippspiel (aus, Worlds, Intern, ein)_.";
      new Query<>(DiscordUser.class).where(Condition.Comparer.GREATER_EQUAL, "tippspiel", 2)
          .entityList().stream().filter(user -> user.getBet(match) == null).forEach(user -> user.dm(message));
    }
  }
}
