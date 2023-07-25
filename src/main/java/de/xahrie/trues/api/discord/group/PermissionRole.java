package de.xahrie.trues.api.discord.group;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class PermissionRole {
  public static final int NO_ROLES = -1;
  public static final int EVERYONE = 0;
  public static final int REGISTERED = 1;
  public static final int APPLICANT = 2;
  public static final int TRYOUT = 3;
  public static final int SUBSTITUDE = 4;
  public static final int EVENT_PLANNING = 5;
  public static final int CASTER = 6;
  public static final int DEVELOPER = 7;
  public static final int SOCIAL_MEDIA = 8;
  public static final int PLAYER = 9;
  public static final int MENTOR = 15;
  public static final int TEAM_CAPTAIN = 18;
  public static final int TEAM_BUILDING = 19;
  public static final int EVENT_MANAGER = 20;
  public static final int ORGA_LEADER = 23;

  public static final int ORGA_MEMBER = 105;
  public static final int LEADER = 106;
  public static final int MANAGEMENT = 107;
  public static final int NOT_IN_ORGA = 200;
  public static final int EVENT = 201;
  public static final int TEAMS = 202;
  public static final int COACHING = 203;
  public static final int ALL_DEPARTMENTS = 204;

  public static Set<DiscordGroup> of(Integer id, boolean recursive) {
    if (!recursive || id >= 200) {
      return of(id);
    }
    if (id < 100) {
      final DiscordGroup groupFound = DiscordGroup.of(id);
      final Set<DiscordGroup> groups = new HashSet<>(Set.of(groupFound));
      groups.addAll(groupFound.requires());
      return groups;
    }
    final GroupTier tier = GroupTier.values()[id - 100];
    return tier.requires();
  }

  private static Set<DiscordGroup> of(Integer id) {
    if (id == null) {
      return Set.of();
    }
    if (id >= 1000) {
      return Set.of(DiscordGroup.of(id));
    }
    if (id >= 200) {
      final Department department = Department.values()[id - 200];
      return department.getGroups();
    }
    if (id >= 100) {
      final GroupTier tier = GroupTier.values()[id - 100];
      return tier.getGroups();
    }

    return Set.of(DiscordGroup.of(id));
  }
}
