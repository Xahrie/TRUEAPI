package de.xahrie.trues.api.community.orgateam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import de.xahrie.trues.api.calendar.TeamCalendar;
import de.xahrie.trues.api.database.query.Condition;
import de.xahrie.trues.api.database.query.Query;

public record OrgaTeamScheduler(OrgaTeam team) {
  public List<TeamCalendar> getCalendarEntries() {
    final var limitTime = LocalDateTime.now().minusMinutes(30);
    final List<TeamCalendar> calendarEntries = new Query<>(TeamCalendar.class).where("orga_team", team)
        .and(Condition.Comparer.GREATER_EQUAL, "calendar_end", limitTime)
        .entityList();
    if (team.getTeam() == null) return List.of();
    team.getTeam().getMatches().getUpcomingMatches().stream()
        .map(match -> new TeamCalendar(match.getExpectedTimeRange(), String.valueOf(match.getId()), TeamCalendar.TeamCalendarType.MATCH, team, -1))
        .forEach(calendarEntries::add);

    return calendarEntries;
  }

  public List<TeamCalendar> getCalendarEntries(LocalDate localDate) {
    return getCalendarEntries().stream().filter(calendar -> calendar.getRange().getStartTime().toLocalDate().isEqual(localDate)).toList();
  }
}
