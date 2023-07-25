package de.xahrie.trues.api.discord.command;

import java.util.List;
import java.util.Set;

import de.xahrie.trues.api.discord.command.slash.annotations.Perm;
import de.xahrie.trues.api.discord.group.DiscordGroup;
import de.xahrie.trues.api.discord.group.PermissionRole;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;

public record PermissionCheck(Set<DiscordGroup> groups) {
  public PermissionCheck(Perm perm) {
    this(PermissionRole.of(perm.value(), perm.recursive()));
  }

  public boolean check(Member member) {
    if (groups.isEmpty() && member.getRoles().isEmpty()) {
      return true;
    }
    if (groups.contains(DiscordGroup.EVERYONE)) {
      return true;
    }
    final List<Long> roleIds = groups.stream().map(DiscordGroup::getDiscordId).toList();
    return member.getRoles().stream().map(ISnowflake::getIdLong).anyMatch(roleIds::contains);
  }

}
