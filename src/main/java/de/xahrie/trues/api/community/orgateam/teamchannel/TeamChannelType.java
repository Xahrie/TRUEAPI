package de.xahrie.trues.api.community.orgateam.teamchannel;

import java.util.Arrays;

import de.xahrie.trues.api.database.connector.Listing;
import de.xahrie.trues.api.discord.channel.ChannelType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

@RequiredArgsConstructor
@Getter
@Listing(Listing.ListingType.LOWER)
public enum TeamChannelType {
  CATEGORY(null),
  CHAT("\uD83D\uDCAC︱team-chat"),
  INFO("\uD83D\uDCCB︱team-info"),
  PRACTICE("Practice & PRM"),
  SCOUTING("\uD83D\uDD0E︱scouting"),
  VOICE(null);

  private final String defaultName;

  public ChannelType getPermissionType() {
    return equals(VOICE) ? ChannelType.ORGA_INTERN : ChannelType.TEAM;
  }

  public static TeamChannelType fromChannel(GuildChannel channel) {
    return Arrays.stream(TeamChannelType.values()).filter(teamChannelType -> channel.getName().equals(teamChannelType.getDefaultName()))
        .findFirst().orElse(channel instanceof VoiceChannel ? VOICE : (channel instanceof Category ? CATEGORY : CHAT));
  }
}

