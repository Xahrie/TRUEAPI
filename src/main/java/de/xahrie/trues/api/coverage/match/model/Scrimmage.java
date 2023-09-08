package de.xahrie.trues.api.coverage.match.model;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

import de.xahrie.trues.api.coverage.participator.model.Participator;
import de.xahrie.trues.api.coverage.match.log.EventStatus;
import de.xahrie.trues.api.coverage.match.log.MatchLog;
import de.xahrie.trues.api.coverage.match.log.MatchLogAction;
import de.xahrie.trues.api.coverage.playday.Playday;
import de.xahrie.trues.api.coverage.playday.PlaydayFactory;
import de.xahrie.trues.api.database.connector.SQLUtils;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.database.query.SQLEnum;
import de.xahrie.trues.api.datatypes.calendar.TimeFormat;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Table(value = "coverage", department = "scrimmage")
@ExtensionMethod(SQLUtils.class)
public class Scrimmage extends Match implements Entity<Scrimmage> {
  @Serial
  private static final long serialVersionUID = -6736012840442317674L;

  public Scrimmage(@NotNull LocalDateTime start) {
    this(PlaydayFactory.current(), MatchFormat.TWO_GAMES, start, (short) 0, EventStatus.CREATED,
        "keine Infos", true, "-:-");
  }

  public Scrimmage(@Nullable Playday playday, @NotNull MatchFormat format, @NotNull LocalDateTime start,
                   @Nullable Short rateOffset, @NotNull EventStatus status, @NotNull String lastMessage,
                   boolean active, @NotNull String result) {
    super(playday, format, start, rateOffset, status, lastMessage, active, result);
  }

  private Scrimmage(int id, Integer playdayId, MatchFormat format, LocalDateTime start, Short rateOffset,
                    EventStatus status, String lastMessage, boolean active, String result) {
    super(id, playdayId, format, start, rateOffset, status, lastMessage, active, result);
  }

  public static Scrimmage get(List<Object> objects) {
    return new Scrimmage(
        (int) objects.get(0),
        objects.get(2).intValue(),
        new SQLEnum<>(MatchFormat.class).of(objects.get(3)),
        (LocalDateTime) objects.get(4),
        SQLUtils.shortValue(objects.get(5)),
        new SQLEnum<>(EventStatus.class).of(objects.get(6)),
        (String) objects.get(7),
        (boolean) objects.get(8),
        (String) objects.get(9)
    );
  }

  @Override
  public Scrimmage create() {
    final Scrimmage match = new Query<>(Scrimmage.class)
        .col("matchday", playdayId).col("coverage_format", format).col("coverage_start", start)
        .col("rate_offset", rateOffset).col("status", status).col("last_message", lastMessage)
        .col("active", active).col("result", result)
        .insert(this);
    new MatchLog(this, MatchLogAction.CREATE, "Spiel erstellt", null).create();

    if (match.getParticipators().length == 0) {
      final Participator home = new Participator(match, true).create();
      final Participator guest = new Participator(match, false).create();
      if (home.getId() != 0 && guest.getId() != 0) this.participators = new Participator[]{home, guest};
      else this.participators = null;
    }
    return match;
  }

  @Override
  public String getTypeString() {
    return "Scrimmage";
  }

  public String display() {
    return getId() + " | " + TimeFormat.DEFAULT_FULL.of(getStart()) + " | " + getHomeName() + " vs. " + getGuestName();
  }
}
