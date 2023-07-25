package de.xahrie.trues.api.discord.notify;

import java.time.LocalTime;

import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.coverage.participator.model.Participator;
import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.util.Util;
import lombok.Getter;

@Getter
public class MatchNotifier extends Notifier {
  private final Participator participator;

  public MatchNotifier(LocalTime localTime, DiscordUser discordUser, Participator participator) {
    super(localTime, discordUser);
    this.participator = participator;
  }

  @Override
  public void sendNotification() {
    final OrgaTeam orgaTeam = Util.avoidNull(participator.getTeam(), null, AbstractTeam::getOrgaTeam);
    handleNotification(orgaTeam, "Match " + participator.getMatch().getMatchup(), participator.getMatch().getExpectedTimeRange());
  }
}
