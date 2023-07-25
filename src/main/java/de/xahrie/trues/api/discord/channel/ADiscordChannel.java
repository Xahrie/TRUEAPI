package de.xahrie.trues.api.discord.channel;

import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.community.orgateam.OrgaTeamFactory;
import de.xahrie.trues.api.community.orgateam.teamchannel.TeamChannel;
import de.xahrie.trues.api.community.orgateam.teamchannel.TeamChannelRepository;
import de.xahrie.trues.api.discord.group.DiscordGroup;
import de.xahrie.trues.api.discord.util.Jinx;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ADiscordChannel {
  long getDiscordId();

  DiscordChannelType getChannelType();

  String getName();

  void setName(String name);

  ChannelType getPermissionType();

  void setPermissionType(ChannelType permissionType);

  default void updatePermissions() {
    final IPermissionContainer channel = getChannel();
    final List<ChannelPermissionType.APermissionOverride> permissions = getPermissionType().get(getChannelType()).getPermissions();
    if (permissions == null) return;

    for (ChannelPermissionType.APermissionOverride permission : permissions) {
      final IPermissionHolder permissionHolder = determineHolder(channel, permission.permissionHolder());
      if (permissionHolder == null) continue;

      channel.getManager().putPermissionOverride(permissionHolder, permission.getAllowed(), permission.getDenied()).queue();
    }
  }

  default void updateForGroup(@NotNull DiscordGroup group) {
    final IPermissionHolder holder = group.equals(DiscordGroup.TEAM_ROLE_PLACEHOLDER) ?
        detemineRoleOfChannel((ICategorizableChannel) getChannel()) : determineHolder(getChannel(), group.getRole());
    final ChannelPermissionType.APermissionOverride permission = getPermissionType().get(getChannelType()).getTeamPermission();
    if (holder == null || permission == null) return;

    getChannel().getManager().putPermissionOverride(holder, permission.getAllowed(), permission.getDenied()).queue();
  }

  @Nullable
  private static IPermissionHolder determineHolder(IPermissionContainer channel, IPermissionHolder holder) {
    return holder instanceof Role role && OrgaTeamFactory.isRoleOfTeam(role) && channel instanceof ICategorizableChannel categorizableChannel ?
        detemineRoleOfChannel(categorizableChannel) : holder;
  }

  @Nullable
  private static Role detemineRoleOfChannel(@NotNull ICategorizableChannel categorizableChannel) {
    assert categorizableChannel.getParentCategory() != null;
    final TeamChannel
            teamChannel = TeamChannelRepository.getTeamChannelFromChannel(categorizableChannel.getParentCategory());
    if (teamChannel == null) return null;

    final OrgaTeam team = teamChannel.getOrgaTeam();
    if (team == null) return null;

    return team.getRoleManager().getRole();
  }

  default IPermissionContainer getChannel() {
    return (IPermissionContainer) Jinx.instance.getChannels().getChannel(getDiscordId());
  }
}
