package de.xahrie.trues.api.discord.notify;

import java.time.LocalTime;
import java.util.Objects;

import de.xahrie.trues.api.calendar.TeamCalendar;
import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.discord.user.DiscordUser;
import lombok.Getter;

@Getter
public class TeamEventNotifier extends Notifier {
  private final TeamCalendar teamCalendar;

  public TeamEventNotifier(LocalTime localTime, DiscordUser discordUser, TeamCalendar teamCalendar) {
    super(localTime, discordUser);
    this.teamCalendar = teamCalendar;
  }

  @Override
  public void sendNotification() {
    final OrgaTeam orgaTeam = teamCalendar.getOrgaTeam();
    handleNotification(orgaTeam, "Event " + teamCalendar, teamCalendar.getRange());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final TeamEventNotifier that = (TeamEventNotifier) o;
    return Objects.equals(teamCalendar, that.teamCalendar);
  }

  @Override
  public int hashCode() {
    return Objects.hash(teamCalendar);
  }
}
