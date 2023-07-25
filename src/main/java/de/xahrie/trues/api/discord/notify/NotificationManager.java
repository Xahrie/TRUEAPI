package de.xahrie.trues.api.discord.notify;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import de.xahrie.trues.api.calendar.TeamCalendar;
import de.xahrie.trues.api.community.member.Membership;
import de.xahrie.trues.api.coverage.participator.model.Participator;
import de.xahrie.trues.api.database.query.Condition;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.DateTimeUtils;
import de.xahrie.trues.api.discord.user.DiscordUser;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(DateTimeUtils.class)
public class NotificationManager {
  private static LocalDate day = LocalDate.MIN;
  private static final List<Notifier> notifiers = new ArrayList<>();

  public static void create() {
    day = LocalDate.now();
    notifiers.clear();
    final LocalDateTime localDateTime = LocalDateTime.of(day.plusDays(2), LocalTime.MIN);
    new Query<>(TeamCalendar.class).where(Condition.between("calendar_start", "now()", localDateTime))
                                   .entityList().forEach(NotificationManager::addNotifiersFor);
    new Query<>(Participator.class, "SELECT * FROM coverage_team " +
                                    "LEFT JOIN orga_team ot on coverage_team.team = ot.team INNER JOIN coverage c on coverage_team.coverage = c.coverage_id " +
                                    "WHERE orga_team_id is not null and coverage_start between now() and ?")
        .entityList(List.of(localDateTime)).forEach(NotificationManager::addNotifiersFor);
  }

  public static void addNotifiersFor(TeamCalendar calendar) {
    final List<Notifier> existing = notifiers.stream().filter(notifier -> notifier instanceof TeamEventNotifier teamEventNotifier && teamEventNotifier.getTeamCalendar().equals(calendar)).toList();
    notifiers.removeAll(existing);
    calendar.getOrgaTeam().getActiveMemberships().stream().map(Membership::getUser).filter(user -> user.getNotification() >= 0).forEach(user -> {
      notifiers.add(new TeamEventNotifier(calendar.getRange().getStartTime().toLocalTime(), user, calendar));
      if (user.getNotification() > 0) notifiers.add(new TeamEventNotifier(calendar.getRange().getStartTime().toLocalTime().minusMinutes(user.getNotification()), user, calendar));
    });
  }

  public static void addNotifiersFor(Participator participator) {
    final List<Notifier> existing = notifiers.stream().filter(notifier -> notifier instanceof MatchNotifier teamEventNotifier && teamEventNotifier.getParticipator().equals(participator)).toList();
    notifiers.removeAll(existing);
    participator.getTeam().getOrgaTeam().getActiveMemberships().stream().map(Membership::getUser).filter(user -> user.getNotification() >= 0).forEach(user -> {
      notifiers.add(new MatchNotifier(participator.getMatch().getStart().toLocalTime(), user, participator));
      if (user.getNotification() > 0) notifiers.add(new MatchNotifier(participator.getMatch().getStart().toLocalTime().minusMinutes(user.getNotification()), user, participator));
    });
  }

  public static void addNotifiersFor(DiscordUser user, Integer difference) {
    final List<Notifier> existing = notifiers.stream().filter(notifier -> notifier.getDiscordUser().equals(user)).toList();
    notifiers.removeAll(existing);
    if (difference != null) {
      for (final Notifier notifier : existing) {
        notifier.setLocalTime(notifier.getLocalTime().plusMinutes(difference));
        notifiers.add(notifier);
      }
    }

  }

  public static void sendNotifications() {
    final List<Notifier> current = notifiers.stream().filter(notifier -> notifier.getLocalTime().isBeforeEqual(LocalTime.now())).toList();
    current.forEach(Notifier::sendNotification);
    notifiers.removeAll(current);
  }

  public static LocalDate getDay() {
    return day;
  }
}
