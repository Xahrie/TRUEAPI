package de.xahrie.trues.api.calendar;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.coverage.match.model.Match;
import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import de.xahrie.trues.api.database.connector.DTO;
import de.xahrie.trues.api.database.connector.Listing;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.database.query.SQLEnum;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;
import de.xahrie.trues.api.discord.notify.NotificationManager;
import de.xahrie.trues.api.util.StringUtils;
import de.xahrie.trues.api.util.Util;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@Table(value = "calendar", department = "team")
@ExtensionMethod(StringUtils.class)
public class TeamCalendar extends AbstractEventCalendar implements Entity<TeamCalendar>,
        DTO<Calendar> {
  @Serial private static final long serialVersionUID = -8449986995823183145L;

  private TeamCalendarType type; // calendar_type
  private int orgaTeamId; // orga_team

  public TeamCalendar(@NotNull
  TimeRange timeRange, @NotNull String details, @NotNull TeamCalendarType type, @NotNull
  OrgaTeam orgaTeam,
                      long threadId) {
    super(timeRange, details, threadId);
    this.type = type;
    this.orgaTeam = orgaTeam;
    this.orgaTeamId = orgaTeam.getId();
    this.threadId = threadId;
  }

  private TeamCalendar(int id, TimeRange range, String details, Long threadId, TeamCalendarType type, int orgaTeamId) {
    super(id, range, details, threadId);
    this.type = type;
    this.orgaTeamId = orgaTeamId;
  }

  public static TeamCalendar get(List<Object> objects) {
    return new TeamCalendar(
        (int) objects.get(0),
        new TimeRange((LocalDateTime) objects.get(2), (LocalDateTime) objects.get(3)),
        (String) objects.get(4),
        (Long) objects.get(6),
        new SQLEnum<>(TeamCalendarType.class).of(objects.get(5)),
        (int) objects.get(8)
    );
  }

  @Override
  public TeamCalendar create() {
    final var calendar = new Query<>(TeamCalendar.class)
        .col("calendar_start", range.getStartTime()).col("calendar_end", range.getEndTime()).col("details", details)
        .col("thread_id", threadId).col("calendar_type", type).col("orga_team", orgaTeamId)
        .insert(this);
    if (range.getStartTime().isBefore(LocalDateTime.now().plusDays(1))) NotificationManager.addNotifiersFor(calendar);
    return calendar;
  }

  @Override
  public void setRange(TimeRange range) {
    if (getRange().getStartTime().equals(range.getStartTime())) return;
    if (range.getStartTime().isBefore(LocalDateTime.now().plusDays(1))) NotificationManager.addNotifiersFor(this);
    super.setRange(range);
  }

  @Nullable
  public Match getMatch() {
    final String details = getDetails();
    if (details == null) return null;

    final Integer matchId = getDetails().intValue();
    if (matchId == -1) return null;

    return new Query<>(Match.class).entity(matchId);
  }

  @Override
  public List<Object> getData() {
    final Match match = getMatch();
    return List.of(
        getRange().display(),
        match == null ? type.toString() : match.getTypeString(),
        match == null ? Util.avoidNull(getDetails(), "no data") : Util.avoidNull(match.getOpponentOf(getOrgaTeam().getTeam()), "kein Gegner", AbstractTeam::getName)
    );
  }

  public String toString() {
    final Match match = getMatch();
    return match == null ? type.toString() : match.getTypeString();
  }

  private OrgaTeam orgaTeam;

  public OrgaTeam getOrgaTeam() {
    if (orgaTeam == null) this.orgaTeam = new Query<>(OrgaTeam.class).entity(orgaTeamId);
    return orgaTeam;
  }

  @ExtensionMethod(StringUtils.class)
  @Listing(Listing.ListingType.UPPER)
  public enum TeamCalendarType {
    KALIBRIERUNG, COACHING, CLASH, MEETING, TRAINING, MATCH;

    @Override
    public String toString() {
      return name().capitalizeFirst();
    }
  }
}
