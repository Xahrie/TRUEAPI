package de.xahrie.trues.api.discord.notify;

import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.community.orgateam.teamchannel.TeamChannel;
import de.xahrie.trues.api.community.orgateam.teamchannel.TeamChannelRepository;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.discord.util.Jinx;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalTime;
import java.util.Objects;

@AllArgsConstructor
@Getter
public abstract class Notifier {
  @Setter
  private LocalTime localTime;
  private final DiscordUser discordUser;

  public abstract void sendNotification();

  protected void handleNotification(@Nullable
  OrgaTeam orgaTeam, @NotNull String output, @NotNull
  TimeRange timeRange) {
    if (orgaTeam == null) return;

    final AudioChannel currentVoice = Jinx.instance.getChannels().getVoiceChannel(discordUser.getMember());
    if (currentVoice == null) return;

    final TeamChannel teamChannel = TeamChannelRepository.getTeamChannelFromChannel(currentVoice);
    if (teamChannel != null && orgaTeam.equals(teamChannel.getOrgaTeam())) return;

    getDiscordUser().dm(output + ": " + timeRange.displayRange());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final Notifier notifier = (Notifier) o;
    return Objects.equals(localTime, notifier.localTime) && Objects.equals(discordUser, notifier.discordUser);
  }

  @Override
  public int hashCode() {
    return Objects.hash(localTime, discordUser);
  }
}
