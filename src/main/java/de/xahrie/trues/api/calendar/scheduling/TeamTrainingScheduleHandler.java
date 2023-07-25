package de.xahrie.trues.api.calendar.scheduling;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import de.xahrie.trues.api.community.application.TeamPosition;
import de.xahrie.trues.api.community.application.TeamRole;
import de.xahrie.trues.api.community.member.Membership;
import de.xahrie.trues.api.community.member.MembershipFactory;
import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;
import de.xahrie.trues.api.datatypes.calendar.TimeRanges;
import de.xahrie.trues.api.datatypes.collections.SortedList;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.util.Util;

public record TeamTrainingScheduleHandler(OrgaTeam team) {

  public List<List<String>> ofWeekStarting(LocalDate day) {
    final List<List<String>> times = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      final TeamPosition position = TeamPosition.values()[i];
      final Membership membership = team.getMembership(TeamRole.MAIN, position);
      day = day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

      if (membership != null) {
        final DiscordUser user = membership.getUser();
        final Map<TimeRanges, List<DayOfWeek>> weekdays = new HashMap<>();
        for (int j = 0; j < 7; j++) {
          final DayOfWeek dayOfWeek = DayOfWeek.of(j + 1);
          final List<TimeRange> remaining = new SchedulingHandler(user).getRemainingAt(day.plusDays(j));
          final TimeRanges timeRanges = new TimeRanges(remaining);
          final TimeRanges matchingRanges = weekdays.keySet().stream().filter(tR -> tR.matches(timeRanges)).findFirst().orElse(null);
          if (matchingRanges == null) weekdays.put(timeRanges, new ArrayList<>(List.of(dayOfWeek)));
          else weekdays.get(matchingRanges).add(dayOfWeek);
        }
        final List<String> weekdayStrings = new ArrayList<>();
        weekdays.forEach(((timeRanges, dayOfWeeks) -> {
          final StringBuilder days = new StringBuilder();
          boolean until = false;
          DayOfWeek last = null;
          for (final DayOfWeek dayOfWeek : dayOfWeeks.stream().sorted().toList()) {
            if (last == null) days.append(dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.GERMANY));
            else if (last.getValue() + 1 == dayOfWeek.getValue()) until = true;
            else {
              if (until) days.append("-").append(last.getDisplayName(TextStyle.SHORT, Locale.GERMANY));
              days.append(",").append(dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.GERMANY));
              until = false;
            }
            last = dayOfWeek;
          }
          if (last != null) days.append(until ? "-" : ",").append(last.getDisplayName(TextStyle.SHORT, Locale.GERMANY));


          final String time = timeRanges.ranges().stream().map(TimeRange::toString).collect(Collectors.joining(", "));
          weekdayStrings.add(days + " " + time);
        }));
        final String key = Util.avoidNull(membership, position.toString(), msh -> msh.getUser().getNickname());
        final String value = String.join(", ", weekdayStrings);
        times.add(List.of(key, value));
      }
    }
    return times;
  }

  public List<List<String>> ofDay(LocalDate day) {
    final List<List<String>> times = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      final TeamPosition position = TeamPosition.values()[i];
      final Membership membership = team.getMembership(TeamRole.MAIN, position);
      final List<TimeRange> remainingAt = new ArrayList<>();
      if (membership != null) {
        final DiscordUser user = membership.getUser();
        remainingAt.addAll(new SchedulingHandler(user).getRemainingAt(day));
      }
      String key = position.toString();
      final String value;
      if (remainingAt.isEmpty()) {
        final List<DiscordUser> otherMembers = determinePossibleMembers(position, day);
        value = otherMembers.subList(0, Math.min(3, otherMembers.size())).stream().map(DiscordUser::getNickname).collect(Collectors.joining(", "));
      } else {
        key = membership.getUser().getNickname();
        value = remainingAt.stream().map(TimeRange::toDayString).collect(Collectors.joining(", "));
      }
      if (!value.isBlank()) times.add(List.of(key, value));
    }
    return times;
  }

  private List<DiscordUser> determinePossibleMembers(TeamPosition position, LocalDate day) {
    return MembershipFactory.getOfPosition(position).stream().map(Membership::getUser)
                            .filter(user -> !(new SchedulingHandler(user).getFreeAt(day).isEmpty())).toList();
  }


  public List<TimeRange> getTeamAvailabilitySince(LocalDate date) {
    List<TimeRange> remainingRanges = SortedList.sorted();
    for (int i = 0; i < 5; i++) {
      final TeamPosition position = TeamPosition.values()[i];
      final Membership membership = team.getMembership(TeamRole.MAIN, position);
      if (membership == null) return List.of();

      final List<TimeRange> remaining = new ArrayList<>(new SchedulingHandler(membership.getUser()).getRemaining(date));
      if (remaining.isEmpty()) return List.of();

      if (remainingRanges.isEmpty()) remainingRanges.addAll(remaining);
      else remainingRanges = TimeRange.intersect(remainingRanges, remaining);
    }
    return remainingRanges.subList(0, Math.min(10, remainingRanges.size()));
  }

}
