package de.xahrie.trues.api.discord.channel;

import java.util.List;

import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.community.orgateam.OrgaTeamFactory;
import de.xahrie.trues.api.community.orgateam.teamchannel.TeamChannelType;
import de.xahrie.trues.api.database.connector.Listing;
import de.xahrie.trues.api.util.Util;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;

@Listing(Listing.ListingType.LOWER)
public enum DiscordChannelType {
  TEXT,
  PRIVATE,
  VOICE,
  GROUP,
  CATEGORY,
  NEWS,
  STAGE,
  GUILD_NEWS_THREAD,
  GUILD_PUBLIC_THREAD,
  GUILD_PRIVATE_THREAD,
  FORUM,
  UNKNOWN;

  public void createChannel(String name, Category category, ChannelType type) {
    getAction(name, category, type, null).queue();
  }

  public void createTeamChannel(Category category, TeamChannelType teamChannelType, OrgaTeam team) {
    getAction(teamChannelType.getDefaultName(), category, teamChannelType.getPermissionType(), team).queue();
  }

  private ChannelAction<? extends GuildChannel> getAction(String name, Category category, ChannelType type, OrgaTeam team) {
    final ChannelAction<? extends GuildChannel> channelAction = switch (this) {
      case FORUM -> Util.nonNull(category).createForumChannel(name);
      case NEWS -> Util.nonNull(category).createNewsChannel(name);
      case STAGE -> Util.nonNull(category).createStageChannel(name);
      case TEXT -> Util.nonNull(category).createTextChannel(name);
      case VOICE -> Util.nonNull(category).createVoiceChannel(name);
      default -> throw new IllegalArgumentException("Suspicious type");
    };

    channelAction.clearPermissionOverrides().queue();
    final List<ChannelPermissionType.APermissionOverride> permissions = type.get(this).getPermissions(team);
    assert permissions != null;
    for (ChannelPermissionType.APermissionOverride permission : permissions) {
      IPermissionHolder permissionHolder = permission.holder();
      if (permissionHolder instanceof Role role && OrgaTeamFactory.isRoleOfTeam(role)) {
        permissionHolder = team.getRoleManager().getRole();
      }

      channelAction.addPermissionOverride(permissionHolder, permission.allowed(), permission.denied()).queue();
    }
    return channelAction;
  }
}
