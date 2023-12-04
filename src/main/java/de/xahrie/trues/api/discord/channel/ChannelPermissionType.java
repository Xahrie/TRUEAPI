package de.xahrie.trues.api.discord.channel;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.xahrie.trues.api.community.member.Membership;
import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.database.connector.Listing;
import de.xahrie.trues.api.datatypes.collections.SortedList;
import de.xahrie.trues.api.discord.group.DiscordGroup;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.discord.util.Jinx;
import de.xahrie.trues.api.util.StringUtils;
import de.xahrie.trues.api.util.Util;
import de.xahrie.trues.api.util.io.cfg.JSON;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

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

  @Nullable
  public List<APermissionOverride> getPermissions(@Nullable OrgaTeam team) {
    return getOverrides(team, false);
  }

  @Nullable
  public List<APermissionOverride> getLeaderPermissions(@Nullable OrgaTeam team) {
    return getOverrides(team, true);
  }

  private List<APermissionOverride> getOverrides(@Nullable OrgaTeam team, boolean leaderOnly) {
    if (channelId == null) return null;

    final Map<IPermissionHolder, APermissionOverride> overrides = new HashMap<>();
    final var json = JSON.read("apis.json");
    final var bot = json.getJSONObject("discord");
    final var permData = bot.getJSONObject("permissions");
    final JSONObject perms = permData.getJSONObject(name());
    for (String permName : perms.keySet()) {
      Permission permission = Permission.valueOf(permName.toUpperCase());
      final JSONObject permInfo = perms.getJSONObject(permName);
      for (String roleName : permInfo.keySet()) {
        final boolean allowed = permInfo.getBoolean(roleName);
        final List<IPermissionHolder> holders = leaderOnly ?
            determineLeaderPerms(roleName, team) : determineHolders(roleName, team);
        holders.forEach(holder -> overrides.merge(holder,
            new APermissionOverride(holder, SortedList.of(), SortedList.of()).put(permission, allowed),
            (oldValue, newValue) -> oldValue.put(permission, allowed)
        ));
      }
    }
    return SortedList.of(overrides.values());
  }

  @NotNull
  private List<IPermissionHolder> determineHolders(@NotNull String key, @Nullable OrgaTeam team) {
    long l = key.longValue();
    if (l == -1) {
      if (team != null)
        if (key.equalsIgnoreCase("TEAM"))
          return SortedList.of(team.getRoleManager().getRole());
        else if (key.equalsIgnoreCase("LEADER"))
          return SortedList.of(team.getActiveMemberships().stream().filter(Membership::isCaptain)
              .map(Membership::getUser).filter(Objects::nonNull)
              .map(DiscordUser::getMember).filter(Objects::nonNull).toList());

      l = Util.avoidNull(DiscordGroup.of(key), l, DiscordGroup::getDiscordId);
    }
    return SortedList.of(Jinx.instance.getRoles().getRole(l));
  }

  @NotNull
  private List<IPermissionHolder> determineLeaderPerms(@NotNull String key, @Nullable OrgaTeam team) {
    if (team != null && key.equalsIgnoreCase("LEADER"))
      return SortedList.of(team.getActiveMemberships().stream().filter(Membership::isCaptain)
          .map(Membership::getUser).filter(Objects::nonNull)
          .map(DiscordUser::getMember).filter(Objects::nonNull).toList());
    return SortedList.of();
  }

  public record APermissionOverride(IPermissionHolder holder, SortedList<Permission> allowed,
                                    SortedList<Permission> denied) {
    public APermissionOverride(IPermissionHolder permissionHolder, EnumSet<Permission> allowed, EnumSet<Permission> denied) {
      this(permissionHolder, SortedList.of(allowed), SortedList.of(denied));
    }

    public APermissionOverride add(Permission permission) {
      return put(permission, true);
    }

    public APermissionOverride remove(Permission permission) {
      return put(permission, false);
    }

    public APermissionOverride put(Permission permission, boolean add) {
      if (add) allowed.add(permission);
      else denied.add(permission);
      return this;
    }
  }
}
