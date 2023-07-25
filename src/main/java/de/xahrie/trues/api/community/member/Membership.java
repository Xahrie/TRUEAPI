package de.xahrie.trues.api.community.member;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import de.xahrie.trues.api.community.application.TeamPosition;
import de.xahrie.trues.api.community.application.TeamRole;
import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.database.query.SQLEnum;
import de.xahrie.trues.api.discord.group.RoleGranter;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.util.StringUtils;
import de.xahrie.trues.api.util.Util;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Managed die Aufgabe und Rolle in einem Team <br>
 * Man kann nur einmal pro Team auftreten.
 */
@Getter
@Table("orga_member")
@ExtensionMethod(StringUtils.class)
public class Membership implements Entity<Membership>, Comparable<Membership> {
  @Serial private static final long serialVersionUID = 4053734990350859891L;

  @Setter private int id; // orga_member_id
  private final int userId; // discord_user
  private final Integer orgaTeamId; // orga_team
  private TeamRole role; // role
  private TeamPosition position; // position
  private LocalDateTime timestamp = LocalDateTime.now(); // timestamp
  private boolean captain = false; // captain
  private boolean active = true; // active


  public Membership(@NotNull DiscordUser user, @NotNull TeamPosition position) {
    this(user, null, TeamRole.ORGA, position);
  }

  Membership(@NotNull DiscordUser user, @Nullable
  OrgaTeam team, @NotNull TeamRole role, @NotNull TeamPosition position) {
    this.user = user;
    this.userId = user.getId();
    this.orgaTeam = team;
    this.orgaTeamId = Util.avoidNull(team, OrgaTeam::getId);
    this.role = role;
    this.position = position;
  }

  private Membership(int id, int userId, Integer orgaTeamId, TeamRole role, TeamPosition position, LocalDateTime timestamp,
                     boolean captain, boolean active) {
    this.id = id;
    this.userId = userId;
    this.orgaTeamId = orgaTeamId;
    this.role = role;
    this.position = position;
    this.timestamp = timestamp;
    this.captain = captain;
    this.active = active;
  }

  public static Membership get(List<Object> objects) {
    return new Membership(
        (int) objects.get(0),
        (int) objects.get(1),
        (Integer) objects.get(2),
        new SQLEnum<>(TeamRole.class).of(objects.get(3)),
        new SQLEnum<>(TeamPosition.class).of(objects.get(4)),
        (LocalDateTime) objects.get(5),
        (boolean) objects.get(6),
        (boolean) objects.get(7)
    );
  }

  @Override
  public Membership create() {
    return new Query<>(Membership.class).key("discord_user", userId).key("orga_team", orgaTeamId)
        .col("position", position).col("role", role).col("timestamp", timestamp).col("captain", captain).col("active", active)
        .insert(this);
  }

  public void removeFromTeam() {
    this.active = false;
    new Query<>(Membership.class).col("active", false).update(id);
    new RoleGranter(getUser()).removeTeamRole(this, getOrgaTeam());
    getUser().dm("Du wurdest aus dem Team **" + getOrgaTeam().getName() + "** entfernt. Du kannst aber jederzeit gerne eine neue " +
        "Bewerbung schreiben. Solltest du Probleme oder Fragen haben kannst du mir jederzeit schreiben.");
  }

  public void setCaptain(boolean captain) {
    if (this.captain != captain) new Query<>(Membership.class).col("captain", captain).update(id);
    this.captain = captain;
    new RoleGranter(getUser()).handleCaptain(captain);
  }

  public void updateRoleAndPosition(TeamRole role, TeamPosition position) {
    this.role = role;
    this.position = position;
    this.active = true;
    new Query<>(Membership.class).col("role", role).col("position", position).col("active", true).update(id);
  }

  public void setRole(TeamRole role) {
    this.role = role;
    new Query<>(Membership.class).col("role", role).update(id);
  }

  public String getPositionString() {
    return (role.equals(TeamRole.MAIN) ? "" : role.name().capitalizeFirst() + " ") + position.name().capitalizeFirst();
  }

  @Override
  public int compareTo(@NotNull Membership o) {
    return Comparator.comparing(Membership::isActive)
        .thenComparing(Membership::getRole, Comparator.reverseOrder())
        .thenComparing(Membership::getPosition).compare(this, o);
  }

  private DiscordUser user;

  public DiscordUser getUser() {
    if (user == null) this.user = new Query<>(DiscordUser.class).entity(userId);
    return user;
  }

  private OrgaTeam orgaTeam;

  public OrgaTeam getOrgaTeam() {
    if (orgaTeam == null) this.orgaTeam = new Query<>(OrgaTeam.class).entity(orgaTeamId);
    return orgaTeam;
  }
}
