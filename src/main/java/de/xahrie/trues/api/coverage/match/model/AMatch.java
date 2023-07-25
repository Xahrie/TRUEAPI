package de.xahrie.trues.api.coverage.match.model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import de.xahrie.trues.api.coverage.participator.model.Participator;
import de.xahrie.trues.api.calendar.MatchCalendar;
import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.coverage.ABetable;
import de.xahrie.trues.api.coverage.match.MatchResult;
import de.xahrie.trues.api.coverage.match.log.EventStatus;
import de.xahrie.trues.api.coverage.match.log.MatchLog;
import de.xahrie.trues.api.coverage.playday.Playday;
import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;
import de.xahrie.trues.api.discord.notify.NotificationManager;
import org.jetbrains.annotations.Nullable;

public interface AMatch extends ABetable {
  Playday getPlayday(); // matchday
  MatchFormat getFormat(); // coverage_format
  LocalDateTime getStart(); // coverage_start
  void setStart(LocalDateTime start);
  Short getRateOffset(); // rate_offset
  EventStatus getStatus(); // status
  void setStatus(EventStatus status);
  String getLastMessage(); // last_message
  void setLastMessage(String lastMessage);
  boolean isActive(); // active
  MatchResult getResult(); // result
  Participator[] getParticipators();
  List<MatchLog> getLogs();
  MatchCalendar asEvent();

  default List<MatchLog> determineLog() {
    return new Query<>(MatchLog.class).where("coverage", this).descending("log_time").entityList();
  }

  default void handleNotifications() {
    if (getStart().isBefore(LocalDateTime.now().plusDays(1))) {
      Arrays.stream(getParticipators()).filter(participator -> participator.getTeam() != null)
          .filter(participator -> participator.getTeam().getOrgaTeam() != null).forEach(NotificationManager::addNotifiersFor);
    }
  }

  default Participator getHome() {
    return getParticipators().length > 0 ? getParticipators()[0] : null;
  }

  default String getHomeAbbr() {
    return getHome() == null ? "null" : getHome().getAbbreviation();
  }

  default String getHomeName() {
    return getHome() == null ? "null" : getHome().getName();
  }

  default String getMatchup() {
    return getHomeName() + " vs. " + getGuestName();
  }

  default Participator getGuest() {
    return getParticipators().length > 1 ? getParticipators()[1] : null;
  }

  default String getGuestAbbr() {
    return getGuest() == null ? "null" : getGuest().getAbbreviation();
  }

  default String getGuestName() {
    return getGuest() == null ? "null" : getGuest().getName();
  }

  MatchResult getExpectedResult();
  String getExpectedResultString();

  default Participator getOpponent(AbstractTeam team) {
    final Participator participator = getParticipator(team);
    return participator == null ? null : getParticipator(!participator.isHome());
  }

  default Participator getParticipator(@Nullable AbstractTeam team) {
    if (team == null) return null;
    return Arrays.stream(getParticipators())
        .filter(participator -> participator.getTeam() != null)
        .filter(participator -> participator.getTeam().getId() == team.getId())
        .findFirst().orElse(null);
  }

  default AbstractTeam getOpponentOf(AbstractTeam team) {
    return getOpponent(team).getTeam();
  }

  default List<OrgaTeam> getOrgaTeams() {
    return Arrays.stream(getParticipators())
        .map(Participator::getTeam).filter(Objects::nonNull)
        .map(AbstractTeam::getOrgaTeam).filter(Objects::nonNull).toList();
  }

  default boolean isOrgagame() {
    return !getOrgaTeams().isEmpty();
  }

  default Participator getParticipator(boolean home) {
    return getParticipators()[home ? 0 : 1];
  }

  /**
   * Wenn bereits vorhanden aber nicht für dieses Team lösche
   * @return False, wenn bereits an dieser Stelle vorhanden
   */
  default boolean checkAddParticipatingTeam(Participator participator, @Nullable AbstractTeam team) {
    final Participator currentParticipator = getParticipator(team);
    if (currentParticipator == null) return true;
    if (currentParticipator.isHome() == participator.isHome()) return false;
    currentParticipator.delete();
    return true;
  }

  default Participator addParticipator(AbstractTeam team, boolean home, AbstractTeam other) {
    final Participator existing = getParticipator(team);
    if (existing != null) return getParticipator(team);

    if (!(this instanceof Scrimmage) && isOrgagame()) {
      if (team.getOrgaTeam() == null) team.setRefresh(getStart());
      if (this instanceof PRMMatch) List.of(team, other).forEach(t -> t.setHighlight(true));
    }
    getParticipator(home).setTeam(team);
    handleNotifications();

    return getParticipator(home);
  }

  String getTypeString();

  default boolean isRunning() {
    return getResult().toString().equals("-:-");
  }

  default TimeRange getExpectedTimeRange() {
    return new TimeRange(getStart(), getFormat().getPRMDuration());
  }
}
