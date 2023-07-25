package de.xahrie.trues.api.coverage.team.model;

import de.xahrie.trues.api.community.application.TeamPosition;
import de.xahrie.trues.api.community.application.TeamRole;
import de.xahrie.trues.api.community.member.Membership;
import de.xahrie.trues.api.community.member.MembershipFactory;
import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.discord.group.RoleGranter;
import de.xahrie.trues.api.discord.user.DiscordUser;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import net.dv8tion.jda.api.entities.Role;

@RequiredArgsConstructor
@ExtensionMethod(MembershipFactory.class)
@Deprecated
public class TeamRoleHandler {
  private final AbstractTeam team;
  private OrgaTeam orgaTeam;

  public OrgaTeam getOrgaTeam() {
    if (orgaTeam != null) this.orgaTeam = team.getOrgaTeam();
    return orgaTeam;
  }

  public void addRole(DiscordUser user, TeamRole role, TeamPosition position) {
    if (role.equals(TeamRole.MAIN)) checkMainOnPosition(position);
    final Membership member = orgaTeam.getMember(user, role, position);
    new RoleGranter(member.getUser()).addTeamRole(role, position, orgaTeam);
  }

  private void checkMainOnPosition(TeamPosition position) {
    final Membership mainOnPosition = orgaTeam.getMembership(TeamRole.MAIN, position);
    if (mainOnPosition != null) mainOnPosition.setRole(TeamRole.SUBSTITUTE);
  }

  public void addCaptain(DiscordUser user) {
    final Membership membership = MembershipFactory.getMembershipOf(user, orgaTeam);
    membership.setCaptain(true);
  }

  public void removeCaptain(DiscordUser user) {
    final Membership membership = MembershipFactory.getMembershipOf(user, orgaTeam);
    membership.setCaptain(false);
  }

  public void removeRole(DiscordUser user) {
    final Membership membership = MembershipFactory.getMembershipOf(user, orgaTeam);
    if (membership == null) return;
    membership.removeFromTeam();
  }

  public Role getRole() {
    return orgaTeam.getGroup().determineRole();
  }

  public String getRoleName() {
    return "TRUE " + roleNameFromString(team.getName());
  }

  private String roleNameFromString(String name) {
    return "TRUE " + name.replace("Technical Really Unique ", "")
        .replace("Technical Really ", "")
        .replace("TRUEsports ", "")
        .replace("TRUE ", "");
  }

  public void updateRoleName(String newName) {
    final Role role = getRole();
    role.getManager().setName(newName).queue();
  }
}
