package de.xahrie.trues.api.discord.group;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum GroupTier {
  EVERYONE(10),
  REGISTERED(20),
  APPLICANT(30),
  TRYOUT(40),
  SUBSTITUTE(50),
  ORGA_MEMBER(60),
  LEADER(70),
  MANAGEMENT(80),
  ORGA_LEADER(90);

  private final int permissionId;

  /**
   * Bearbeitbare Rollen (via Rollen bearbeiten)
   */
  public Set<DiscordGroup> getAssignable() {
    return switch (this) {
      case LEADER, MANAGEMENT, ORGA_LEADER -> Set.of(DiscordGroup.FRIEND, DiscordGroup.SCRIMPARTNER);
      default -> Set.of();
    };
  }

  public boolean isOrga() {
    return permissionId >= SUBSTITUTE.getPermissionId();
  }

  public boolean isStaff() {
    return permissionId >= LEADER.getPermissionId();
  }

  public boolean isAdmin() {
    return permissionId >= MANAGEMENT.getPermissionId();
  }

  public Set<DiscordGroup> getInheritedGroups() {
    final var groups = new HashSet<DiscordGroup>();
    if (isOrga()) {
      groups.add(DiscordGroup.ORGA_MEMBER);
      if (isStaff()) {
        groups.add(DiscordGroup.STAFF);
        if (isAdmin()) {
          groups.add(DiscordGroup.ADMIN);
        }
      }
    }
    return groups;
  }

  public Set<DiscordGroup> getGroups() {
    final Stream<DiscordGroup> groups = Arrays.stream(DiscordGroup.values());
    return groups.filter(group -> group.getTier().equals(this))
        .collect(Collectors.toSet());
  }

  public Set<DiscordGroup> requires() {
    final Stream<DiscordGroup> groups = Arrays.stream(DiscordGroup.values());
    return groups.filter(this::isNotBelow).collect(Collectors.toSet());
  }

  private boolean isNotBelow(DiscordGroup group) {
    return group.getTier().getPermissionId() >= permissionId;
  }

  public DiscordGroup getGroup() {
    return DiscordGroup.valueOf(name());
  }
}
