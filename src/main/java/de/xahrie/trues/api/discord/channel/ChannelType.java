package de.xahrie.trues.api.discord.channel;

import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.community.orgateam.OrgaTeamFactory;
import de.xahrie.trues.api.community.orgateam.teamchannel.TeamChannel;
import de.xahrie.trues.api.community.orgateam.teamchannel.TeamChannelType;
import de.xahrie.trues.api.database.connector.Listing;
import de.xahrie.trues.api.util.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
@Getter
@Listing(Listing.ListingType.LOWER)
@ExtensionMethod(StringUtils.class)
public enum ChannelType {
  PUBLIC(ChannelPermissionType.PUBLIC, null, null),
  SOCIALS(ChannelPermissionType.SOCIALS, null, null),
  EVENTS(ChannelPermissionType.EVENTS, null, null),
  LEADERBOARD(ChannelPermissionType.LEADERBOARD, null, null),
  ORGA_INTERN(ChannelPermissionType.ORGA_INTERN, null, ChannelPermissionType.ORGA_INTERN_VOICE),
  STAFF_INTERN(ChannelPermissionType.STAFF_INTERN, null, null), // ???
  CONTENT_INTERN(ChannelPermissionType.CONTENT_INTERN, null, ChannelPermissionType.CONTENT_INTERN_VOICE),
  SUPPORT_TICKET(ChannelPermissionType.SUPPORT_TICKET, null, null),
  TEAM(ChannelPermissionType.TEAM_CATEGORY, ChannelPermissionType.TEAM_CHAT, ChannelPermissionType.TEAM_VOICE);

  private final ChannelPermissionType category;
  private final ChannelPermissionType chat;
  private final ChannelPermissionType voice;

  public ChannelPermissionType getCategory() {
    return category;
  }

  public ChannelPermissionType getChat() {
    return chat == null ? category : chat;
  }

  public ChannelPermissionType getVoice() {
    return voice == null ? category : voice;
  }

  public ChannelPermissionType get(@NotNull DiscordChannelType channelType) {
    return switch (channelType) {
      case CATEGORY -> getCategory();
      case TEXT, NEWS, FORUM -> getChat();
      case VOICE, STAGE -> getVoice();
      default -> ChannelPermissionType.NO_CHANGES;
    };
  }

  /**
   * Channel ist noch nicht in der Datenbank
   */
  public static ChannelType fromChannel(GuildChannel initialChannel) {
    if (!(initialChannel instanceof ICategorizableChannel channel)) return PUBLIC;

    final OrgaTeam team = OrgaTeamFactory.getTeamFromChannel(channel);
    if (team != null) {
      final TeamChannel teamChannel = team.getChannels().get(TeamChannelType.PRACTICE);
      return initialChannel instanceof AudioChannel && teamChannel != null && teamChannel.getChannel().getIdLong() != initialChannel.getIdLong() ? ORGA_INTERN : TEAM;
    }

    final Category category = channel.getParentCategory();
    if (category == null) return PUBLIC;

    return switch (category.getName()) {
      case "Social Media" -> SOCIALS;
      case "Events" -> EVENTS;
      case "Orga Intern" -> ORGA_INTERN;
      case "Content" -> CONTENT_INTERN;
      case "FAQ" -> SUPPORT_TICKET;
      default -> PUBLIC;
    };
  }
}
