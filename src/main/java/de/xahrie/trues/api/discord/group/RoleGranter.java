package de.xahrie.trues.api.discord.group;

import de.xahrie.trues.api.community.application.TeamPosition;
import de.xahrie.trues.api.community.application.TeamRole;
import de.xahrie.trues.api.community.member.Membership;
import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.logging.TeamLog;
import de.xahrie.trues.api.logging.TeamLogFactory;
import de.xahrie.trues.api.util.Util;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.jetbrains.annotations.NotNull;

public class RoleGranter extends RoleGranterBase {
  public RoleGranter(DiscordUser target) {
    super(target);
  }

  public RoleGranter(DiscordUser target, DiscordUser invoker) {
    super(target, invoker);
  }

  public void addTeamRole(TeamRole role, TeamPosition position, @NotNull
  OrgaTeam team) {
    TeamLogFactory.create(invoker, target, target.getMention() + " ist neuer " +
        Util.avoidNull(position.getEmote().getEmoji(), position.getName(), Emoji::getFormatted) + " (" + role.getName() +
        ") bei **" + team.getName() + "**", TeamLog.TeamLogAction.LINEUP_JOIN, team);
    addTeam(team);
    if (role.equals(TeamRole.TRYOUT)) target.getApplications().tryout(role, position);
    if (role.equals(TeamRole.STANDIN)) new RoleGranter(target).add(DiscordGroup.SUBSTITUTE, 1);
    target.getApplications().updateApplicationStatus();
    updateBasedOnGivenRolesAndMembers();
  }

  public void addOrgaRole(TeamRole role, TeamPosition position) {
    addOrga(position);
    if (role.equals(TeamRole.ORGA_TRYOUT)) add(DiscordGroup.SUBSTITUTE, 14);
    target.getApplications().updateApplicationStatus();
  }

  public void removeTeamRole(Membership member, @NotNull OrgaTeam team) {
    TeamLogFactory.create(invoker, target, target.getMention() + " verlässt " + team.getName(), TeamLog.TeamLogAction.LINEUP_LEAVE, team);
    removeTeam(team);
    target.getApplications().demote(member.getRole(), member.getPosition());
    updateBasedOnGivenRolesAndMembers();
  }

  public void removeOrgaRole(Membership member) {
    removeOrga(member.getPosition());
    target.getApplications().demote(member.getRole(), member.getPosition());
    updateBasedOnGivenRolesAndMembers();
  }


  private void updateBasedOnGivenRolesAndMembers() {
    target.getApplications().updateTeamRoleRole();
    target.getApplications().updateOrgaTeamRole();
  }

  public void handleCaptain(boolean b) {
    if (b) add(DiscordGroup.TEAM_CAPTAIN);
    else target.getApplications().updateTeamRoleRole();
  }
}
