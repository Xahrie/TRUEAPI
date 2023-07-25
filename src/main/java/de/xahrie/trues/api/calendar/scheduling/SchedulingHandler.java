package de.xahrie.trues.api.calendar.scheduling;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.xahrie.trues.api.community.member.Membership;
import de.xahrie.trues.api.database.query.Condition;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.DateTimeUtils;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;
import de.xahrie.trues.api.datatypes.collections.SortedList;
import de.xahrie.trues.api.calendar.Calendar;
import de.xahrie.trues.api.discord.user.DiscordUser;

public record SchedulingHandler(DiscordUser user) {
  public static boolean isRepeat(String input) {
    return Arrays.stream(input.replace("\n", " ").split(" "))
        .allMatch(section -> section.equals("repeat") || section.contains("@")) && input.contains("repeat");
  }

  public void repeat() {
    final LocalDate start = LocalDate.now().minusWeeks(1);
    final LocalDate end = LocalDate.now().minusDays(1);
    new Query<>(SchedulingCalendar.class)
        .where("discord_user", user)
        .and(Condition.Comparer.NOT_EQUAL, "details", "urlaub")
        .and(Condition.between("date(calendar_start)", start, end)).entityList()
        .forEach(schedulingCalendar -> new SchedulingCalendar(schedulingCalendar.getRange().plusWeeks(1), null, user).create());
  }

  public void add(SortedList<TimeRange> ranges) {
    if (ranges == null || ranges.isEmpty()) return;

    delete(ranges);
    TimeRange.combine(ranges).forEach(betterTimeRange -> new SchedulingCalendar(betterTimeRange, null, user).create());
  }

  public void delete(List<TimeRange> ranges) {
    ranges.stream().map(TimeRange::getStartTime).map(LocalDateTime::toLocalDate).distinct().forEach(localDate ->
        new Query<>(SchedulingCalendar.class)
            .where("discord_user", user)
            .and(Condition.Comparer.NOT_EQUAL, "details", "urlaub")
            .and("DATE(calendar_start)", localDate)
            .entityList().forEach(Entity::delete));
  }

  public List<TimeRange> getRemaining(LocalDate from) {
    final List<SchedulingCalendar> all = new Query<>(SchedulingCalendar.class)
        .where("discord_user", user).and(Condition.Comparer.GREATER_EQUAL, "DATE(calendar_start)", from).entityList();
    final List<SchedulingCalendar> reduceable = new Query<>(SchedulingCalendar.class)
        .where("discord_user", user).and("details", "urlaub").entityList();
    return TimeRange.reduce(new ArrayList<>(all.stream().map(SchedulingCalendar::getRange).toList()),
        new ArrayList<>(reduceable.stream().map(SchedulingCalendar::getRange).toList()));
  }

  public List<TimeRange> getRemainingAt(LocalDate at) {
    return getRemaining(at).stream().filter(timeRange -> timeRange.getStartTime().toLocalDate().equals(at)).toList();
  }

  public List<TimeRange> getFreeAt(LocalDate at) {
    final List<TimeRange> availableRanges = new ArrayList<>(getRemainingAt(at));
    final List<TimeRange> blockedRanges = new ArrayList<>();
    user.getMainMemberships().stream().map(Membership::getOrgaTeam)
        .filter(Objects::nonNull).findFirst()
        .ifPresent(orgaTeam ->
            blockedRanges.addAll(orgaTeam.getScheduler().getCalendarEntries(at).stream().map(Calendar::getRange).toList()));
    return TimeRange.reduce(availableRanges, blockedRanges);
  }

  public List<TimeRange> getRemainingFromTo(LocalDate from, LocalDate to) {
    return getRemaining(from).stream().filter(timeRange -> DateTimeUtils.isBetween(timeRange.getStartTime(), from, to)).toList();
  }

  public String getAvailabilities() {
    final List<TimeRange> remaining = getRemaining(LocalDate.now());
    if (remaining.isEmpty()) return "keine Einträge";
    return remaining.stream().map(TimeRange::toString).collect(Collectors.joining("\n"));
  }
}
