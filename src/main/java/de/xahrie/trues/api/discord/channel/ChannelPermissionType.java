package de.xahrie.trues.api.discord.channel;

import de.xahrie.trues.api.community.orgateam.OrgaTeamFactory;
import de.xahrie.trues.api.database.connector.Listing;
import de.xahrie.trues.api.datatypes.collections.SortedList;
import de.xahrie.trues.api.discord.group.DiscordGroup;
import de.xahrie.trues.api.discord.util.Jinx;
import de.xahrie.trues.api.util.StringUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

@RequiredArgsConstructor
@Getter
@Listing(Listing.ListingType.LOWER)
@ExtensionMethod(StringUtils.class)
public enum ChannelPermissionType {
  PUBLIC(1110508527308505158L),
  SOCIALS(1110511082184921119L),
  EVENTS(1110512000175439972L),
  LEADERBOARD(1110514204429013022L),
  ORGA_INTERN(1110519317302349834L),
  ORGA_INTERN_VOICE(1110520608757919765L),
  STAFF_INTERN(1110520957614948433L),
  CONTENT_INTERN(1110522667112615946L),
  CONTENT_INTERN_VOICE(1110522667112615946L),
  SUPPORT_TICKET(1118130338892353667L),
  TEAM_CATEGORY(924429703002079272L),
  TEAM_CHAT(1110525496464248904L),
  TEAM_VOICE(1110523880596062261L),
  NO_CHANGES(null);

  private final Long channelId;

  @NonNull
  private List<Long> getViewableRoles() {
    final GuildChannel channel = Jinx.instance.getChannels().getChannel(channelId);
    if (channel instanceof AudioChannel) return List.of();
    if (channel instanceof StandardGuildMessageChannel messageChannel) return messageChannel.getTopic() == null ? List.of() :
        Arrays.stream(messageChannel.getTopic().before("//").strip().split(",")).map(Long::parseLong).toList();
    throw new IllegalArgumentException("Hier sollte Schluss sein!");
  }

  @Nullable
  public List<APermissionOverride> getPermissions() {
    if (channelId == null) return null;
    return Jinx.instance.getChannels().getChannel(channelId).getPermissionContainer().getPermissionOverrides().stream().map(permissionOverride -> new APermissionOverride(permissionOverride.getPermissionHolder(), permissionOverride.getAllowed(), permissionOverride.getDenied(), getViewableRoles())).toList();
  }

  @Nullable
  public APermissionOverride getTeamPermission() {
    final List<APermissionOverride> permissions = getPermissions();
    if (permissions == null) return null;

    return permissions.stream().filter(permission -> permission.permissionHolder() instanceof Role role
                                                     && OrgaTeamFactory.isRoleOfTeam(role)).findFirst().orElse(null);
  }

  public record APermissionOverride(IPermissionHolder permissionHolder, List<Permission> allowed, List<Permission> denied, List<Long> viewable) {
    public APermissionOverride(IPermissionHolder permissionHolder, EnumSet<Permission> allowed, EnumSet<Permission> denied, List<Long> viewable) {
      this(permissionHolder, SortedList.of(allowed), SortedList.of(denied), viewable);
    }

    public List<Permission> getAllowed() {
      final List<Permission> allowed = SortedList.of(allowed());
      if (viewable.isEmpty()) allowed.remove(Permission.VIEW_CHANNEL);
      else if (viewable.contains(permissionHolder.getIdLong())) allowed.add(Permission.VIEW_CHANNEL);
      return allowed;
    }

    public List<Permission> getDenied() {
      final List<Permission> denied = SortedList.of(denied());
      if (viewable.isEmpty()) denied.remove(Permission.VIEW_CHANNEL);
      else if (permissionHolder.getIdLong() == DiscordGroup.EVERYONE.getDiscordId()) denied.add(Permission.VIEW_CHANNEL);
      return denied;
    }
  }
}
