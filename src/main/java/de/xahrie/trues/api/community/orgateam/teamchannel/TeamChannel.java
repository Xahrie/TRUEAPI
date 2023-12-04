package de.xahrie.trues.api.community.orgateam.teamchannel;

import java.io.Serial;
import java.util.List;
import java.util.Objects;

import de.xahrie.trues.api.community.member.Membership;
import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.community.orgateam.OrgaTeamFactory;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.database.query.SQLEnum;
import de.xahrie.trues.api.discord.channel.AbstractDiscordChannel;
import de.xahrie.trues.api.discord.channel.ChannelPermissionType;
import de.xahrie.trues.api.discord.channel.ChannelType;
import de.xahrie.trues.api.discord.channel.DiscordChannelType;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@Table(value = "discord_channel", department = "team")
public class TeamChannel extends AbstractDiscordChannel implements Entity<TeamChannel> {
  @Serial private static final long serialVersionUID = -1851145520721821488L;

  private final int orgaTeamId; // orga_team
  private final TeamChannelType teamChannelType; // teamchannel_type

  public TeamChannel(long discordId, @NotNull String name, @NotNull ChannelType permissionType, @NotNull DiscordChannelType channelType,
                     @NotNull OrgaTeam orgaTeam, @NotNull TeamChannelType teamChannelType) {
    super(discordId, name, permissionType, channelType);
    this.orgaTeam = orgaTeam;
    this.orgaTeamId = orgaTeam.getId();
    this.teamChannelType = teamChannelType;
  }

  private TeamChannel(int id, long discordId, DiscordChannelType channelType, String name, ChannelType permissionType,
                      int orgaTeamId, TeamChannelType teamChannelType) {
    super(id, discordId, channelType, name, permissionType);
    this.orgaTeamId = orgaTeamId;
    this.teamChannelType = teamChannelType;
  }

  public static TeamChannel get(List<Object> objects) {
    return new TeamChannel(
        (int) objects.get(0),
        (long) objects.get(2),
        new SQLEnum<>(DiscordChannelType.class).of(objects.get(3)),
        (String) objects.get(4),
        new SQLEnum<>(ChannelType.class).of(objects.get(5)),
        (int) objects.get(6),
        new SQLEnum<>(TeamChannelType.class).of(objects.get(7))
    );
  }

  @Override
  public TeamChannel create() {
    return new Query<>(TeamChannel.class).key("discord_id", discordId)
        .col("channel_type", channelType).col("channel_name", name).col("permission_type", permissionType)
        .col("orga_team", orgaTeamId).col("teamchannel_type", teamChannelType)
        .insert(this);
  }

  public void updatePermissions() {
    final IPermissionContainer channel = getChannel();
    final List<ChannelPermissionType.APermissionOverride> permissions =
        getPermissionType().get(getChannelType()).getPermissions(getOrgaTeam());
    if (permissions == null) return;

    for (ChannelPermissionType.APermissionOverride permission : permissions) {
      channel.getManager().putPermissionOverride(permission.holder(), permission.allowed(), permission.denied()).queue();
    }
  }

  public void updateLeaderPermissions() {
    final IPermissionContainer channel = getChannel();
    final List<Membership> leaders = getOrgaTeam().getLeaders();
    for (PermissionOverride memberPermissionOverride : channel.getMemberPermissionOverrides()) {
      final Member member = memberPermissionOverride.getMember();
      if (member == null) continue;
      if (leaders.stream().noneMatch(membership -> membership.getUser().getDiscordId() == member.getIdLong()))
        memberPermissionOverride.delete().queue();
    }

    final List<ChannelPermissionType.APermissionOverride> permissions =
        getPermissionType().get(getChannelType()).getLeaderPermissions(getOrgaTeam());
    if (permissions == null) return;
    for (ChannelPermissionType.APermissionOverride permission : permissions) {
      channel.getManager().putPermissionOverride(permission.holder(), permission.allowed(), permission.denied()).queue();
    }
  }

  private OrgaTeam orgaTeam;

  public OrgaTeam getOrgaTeam() {
    if (orgaTeam == null) this.orgaTeam = new Query<>(OrgaTeam.class).entity(orgaTeamId);
    return orgaTeam;
  }
}
