package de.xahrie.trues.api.community.application;

import de.xahrie.trues.api.community.member.Membership;
import de.xahrie.trues.api.community.member.MembershipFactory;
import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.collections.SortedList;
import de.xahrie.trues.api.discord.group.DiscordGroup;
import de.xahrie.trues.api.discord.group.RoleGranter;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.discord.user.DiscordUserFactory;
import de.xahrie.trues.api.discord.util.DefinedTextChannel;
import de.xahrie.trues.api.discord.util.Jinx;
import de.xahrie.trues.api.logging.ServerLog;
import de.xahrie.trues.api.util.Util;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public record ApplicationHandler(DiscordUser user) {
  public List<Application> list() {
    return new Query<>(Application.class).where("discord_user", user).entityList();
  }

  public List<Application> active() {
    return new Query<>(Application.class).where("discord_user", user).and("waiting", 1).entityList();
  }

  /**
   * Team tryoutet einen Bewerber für ihr Team
   *
   * @return neue Bewerbung
   */
  public Application tryout(@NonNull TeamRole role, @NonNull TeamPosition position) {
    final Application application = new Applyer(role, position).buildApplication();
    new RoleGranter(user).add(DiscordGroup.SUBSTITUTE, 14);
    return application;
  }

  /**
   * Nutzer wird aus dem Team entfernt und immer wieder zum Tryout
   *
   * @return neue Bewerbung
   */
  public Application demote(@NonNull TeamRole role, @NonNull TeamPosition position) {
    return new Applyer(role, position).buildApplication();
  }

  /**
   * Zustaendig für Tryout und Applicant
   * <pre>
   *   Bewerber: User not accepted
   *   Tryout: User accepted
   *   </pre>
   */
  public void updateApplicationStatus() {
    final RoleGranter roleGranter = new RoleGranter(user);
    final boolean isActive = !user.getApplications().active().isEmpty();
    if (user.getAcceptedBy() == null) roleGranter.remove(DiscordGroup.TRYOUT);
    else if (isActive) roleGranter.add(DiscordGroup.TRYOUT);
  }

  /**
   * Zuständig für Teamrollen
   */
  public void updateOrgaTeamRole() {
    final List<Membership> currentTeams = MembershipFactory.getCurrentTeams(user);
    final List<OrgaTeam> currentOrgaTeams = currentTeams.stream().map(Membership::getOrgaTeam).toList();
    final RoleGranter granter = new RoleGranter(user);
    for (OrgaTeam orgaTeam : new Query<>(OrgaTeam.class).entityList()) {
      if (currentOrgaTeams.contains(orgaTeam) && !user.getMember().getRoles().contains(orgaTeam.getRoleManager().getRole())) {
        granter.addTeam(orgaTeam);
        continue;
      }
      if (!currentOrgaTeams.contains(orgaTeam) && user.getMember().getRoles().contains(orgaTeam.getRoleManager().getRole())) {
        granter.removeTeam(orgaTeam);
      }
    }
  }

  /**
   * Zuständig für TeamCaptain, Spieler, Substitute,
   */
  public void updateTeamRoleRole() {
    final List<Membership> currentTeams = MembershipFactory.getCurrentTeams(user);
    final RoleGranter granter = new RoleGranter(user);
    handleTeamRole(granter, currentTeams, DiscordGroup.TEAM_CAPTAIN, Membership::isCaptain);
    handleTeamRole(granter, currentTeams, DiscordGroup.SUBSTITUTE, member -> member.getRole().equals(TeamRole.SUBSTITUTE) || member.getRole().equals(TeamRole.TRYOUT));
    handleTeamRole(granter, currentTeams, DiscordGroup.PLAYER, member -> member.getRole().equals(TeamRole.MAIN) && member.getPosition().ordinal() < 5);
  }

  private static void handleTeamRole(RoleGranter granter, @NotNull List<Membership> teams, DiscordGroup group, Predicate<Membership> predicate) {
    if (teams.stream().anyMatch(predicate)) granter.add(group);
    else granter.remove(group);
  }

  public class Applyer {
    private TeamRole role;
    private final TeamPosition position;
    private String appNotes;
    private final boolean active;

    public Applyer(TeamRole role, TeamPosition position) {
      this(role, position, null, false);
    }

    public Applyer(TeamRole role, TeamPosition position, String appNotes, boolean active) {
      this.role = role;
      this.position = position;
      this.appNotes = appNotes;
      this.active = active;
    }

    /**
     * Erstelle eine neue Bewerbung für eine Position oder update bestehende Bewerbung <br>
     * Sende entsprechende Nachrichten in Channel
     *
     * @return neue Bewerbung
     */
    public Application create(@Nullable ModalInteractionEvent event, boolean sendToChannel) {
      final Application application = buildApplication();
      final Member member = Util.avoidNull(event, ModalInteractionEvent::getMember);
      new ServerLog(Util.avoidNull(member, DiscordUserFactory::getDiscordUser),
          user, application.toString(), ServerLog.ServerLogAction.APPLICATION_CREATED).create();
      final String message = "Neuer Bewerber " + user.getMention() + ": " + application;
      Jinx.instance.getChannels().getTextChannel(DefinedTextChannel.ADMIN_CHANNEL).sendMessage(message).queue();
      if (sendToChannel) Jinx.instance.getChannels().getTextChannel(DefinedTextChannel.BEWERBER).sendMessage(message).queue();
      user.dm("Deine Bewerbung wurde abgeschickt: " + application);
      return application;
    }

    private Application buildApplication() {
      final Application existing = new Query<>(Application.class).where("discord_user").and("position", position).entity();
      if (existing != null) {
        if (SortedList.of(TeamRole.ORGA_TRYOUT, TeamRole.TRYOUT).contains(role)) this.role = existing.getRole();
        if (appNotes == null) this.appNotes = existing.getAppNotes();
      } else if (SortedList.of(TeamRole.ORGA_TRYOUT, TeamRole.TRYOUT).contains(role)) role = position.isOrga() ? TeamRole.ORGA : TeamRole.MAIN;

      final Application application = new Application(user, role, position, active, appNotes).create();
      updateApplicationStatus();
      return application;
    }
  }
}
