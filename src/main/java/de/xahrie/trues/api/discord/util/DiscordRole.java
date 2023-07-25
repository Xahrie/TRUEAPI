package de.xahrie.trues.api.discord.util;

import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class DiscordRole {
  private final Willump willump;

  public DiscordRole(Willump willump) {
    this.willump = willump;
  }

  public Role getRole(OrgaTeam team) {
    return getRole(team.getRoleManager().getRoleName());
  }

  public Role getRole(String name) {
    return willump.guild.getRolesByName(name, true).stream().findFirst().orElse(null);
  }

  public Role getRole(long id) {
    return willump.guild.getRoleById(id);
  }

  public void addRole(Member member, Role role) {
    willump.guild.addRoleToMember(member, role).queue();
  }

  public void removeRole(Member member, Role role) {
    willump.guild.removeRoleFromMember(member, role).queue();
  }
}
