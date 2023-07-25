package de.xahrie.trues.api.calendar;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import de.xahrie.trues.api.coverage.match.Caster;
import de.xahrie.trues.api.coverage.match.model.Match;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.util.StringUtils;
import lombok.Getter;
import lombok.experimental.ExtensionMethod;

@Getter
@Table(value = "calendar", department = "cast")
@ExtensionMethod(StringUtils.class)
public class Cast extends AbstractUserCalendar implements Entity<Cast> {
  @Serial
  private static final long serialVersionUID = 4761293199681163070L;

  private Match match;

  public int getMatchId() {
    return details.intValue();
  }

  public Match getMatch() {
    if (match == null) this.match = new Query<>(Match.class).entity(getMatchId());
    return match;
  }

  public List<Caster> getCaster() {
    return new Query<>(Caster.class).where("cast", id).entityList();
  }

  public Cast(DiscordUser invoker, Match match) {
    this(invoker, match, 10, 10);
  }

  public Cast(DiscordUser invoker, Match match, int preshowDurationInMinutes, int postshowDurationInMinutes) {
    super(new TimeRange(match.getStart().plusMinutes(preshowDurationInMinutes), match.getExpectedTimeRange().getEndTime().plusMinutes(postshowDurationInMinutes)),
        String.valueOf(match.getId()), invoker);
    this.match = match;
  }

  private Cast(int id, TimeRange range, String matchId, int invokerId) {
    super(id, range, matchId, invokerId);
  }

  public static Cast get(List<Object> objects) {
    return new Cast(
        (int) objects.get(0),
        new TimeRange((LocalDateTime) objects.get(2), (LocalDateTime) objects.get(3)),
        (String) objects.get(4),
        (int) objects.get(7)
    );
  }

  @Override
  public Cast create() {
    final var previous = new Query<>(Cast.class).where("details", details).entity();
    if (previous != null) previous.delete();
    return new Query<>(Cast.class).key("details", details)
        .col("calendar_start", range.getStartTime())
        .col("calendar_end", range.getEndTime())
        .col("discord_user", userId)
        .insert(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Cast cast)) return false;
    return getMatchId() == cast.getMatchId();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getMatchId());
  }
}
