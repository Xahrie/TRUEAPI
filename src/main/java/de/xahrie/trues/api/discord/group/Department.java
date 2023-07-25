package de.xahrie.trues.api.discord.group;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Department {
  NONE(200),
  EVENT(201),
  TEAMS(202),
  ALL(210);

  private final Integer id;


  public List<DiscordGroup> getPingableGroups() {
    if (id == 200) return List.of();
    if (id == 210) return List.of(DiscordGroup.EVENT, DiscordGroup.TEAMS);
    return List.of(DiscordGroup.of(id));
  }

  public static Department of(Integer id) {
    if (id == 210) {
      return ALL;
    }
    return Arrays.stream(Department.values()).filter(department -> department.getId().equals(id))
        .findFirst().orElse(null);
  }

  public Set<DiscordGroup> getGroups() {
    final Stream<DiscordGroup> groups = Arrays.stream(DiscordGroup.values());
    return groups.filter(group -> group.getDepartment().equals(this))
        .collect(Collectors.toSet());
  }

  public DiscordGroup getGroup() {
    return DiscordGroup.of(id);
  }
}
