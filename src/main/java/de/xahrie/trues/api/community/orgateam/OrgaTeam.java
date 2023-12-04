package de.xahrie.trues.api.community.orgateam;

import java.io.Serial;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import de.xahrie.trues.api.community.application.TeamPosition;
import de.xahrie.trues.api.community.application.TeamRole;
import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import de.xahrie.trues.api.community.member.Membership;
import de.xahrie.trues.api.database.connector.SQLUtils;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.discord.group.CustomDiscordGroup;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.util.Util;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Table("orga_team")
@ToString
public class OrgaTeam implements Entity<OrgaTeam>, Comparable<OrgaTeam> {
  @Serial
  private static final long serialVersionUID = 5847570695211918386L;
  @Setter
  private int id; // orga_team_id
  private String nameCreation; // team_name_created
  private String abbreviationCreation; // team_abbr_created
  private final int groupId; // team_role
  private Integer teamId; // team
  @Setter
  private Byte place = 0; // orga_place
  @Setter
  private Byte standins = 4; // stand_ins

  public OrgaTeam(@NotNull String nameCreation, @NotNull String abbreviationCreation, @NotNull CustomDiscordGroup group) {
    this.nameCreation = nameCreation;
    this.abbreviationCreation = abbreviationCreation;
    this.group = group;
    this.groupId = group.getId();
  }

  private OrgaTeam(int id, String nameCreation, String abbreviationCreation, int groupId, Integer teamId, Byte place, Byte standins) {
    this.id = id;
    this.nameCreation = nameCreation;
    this.abbreviationCreation = abbreviationCreation;
    this.groupId = groupId;
    this.teamId = teamId;
    this.place = place;
    this.standins = standins;
  }

  public static OrgaTeam get(List<Object> objects) {
    return new OrgaTeam(
        (int) objects.get(0),
        (String) objects.get(1),
        (String) objects.get(2),
        (int) objects.get(3),
        (Integer) objects.get(4),
        SQLUtils.byteValue(objects.get(5)),
        SQLUtils.byteValue(objects.get(6))
    );
  }

  @Nullable
  public static OrgaTeam fromName(@NonNull String name) {
    return new Query<>(OrgaTeam.class).where("team_name_created", name).entity();
  }

  @Override
  public OrgaTeam create() {
    final OrgaTeam orgaTeam = new Query<>(OrgaTeam.class).key("orga_team_id", id)
        .col("team_name_created", nameCreation).col("team_abbr_created", abbreviationCreation).col("team_role", group)
        .col("team", team).col("orga_place", place).col("stand_ins", standins).insert(this);
    if (team != null) team.setOrgaTeam(this);
    return orgaTeam;
  }

  public void setNameCreation(String nameCreation) {
    final boolean updated = !this.nameCreation.equals(nameCreation);
    this.nameCreation = nameCreation;
    if (updated) {
      getRoleManager().updateRoleName(nameCreation);
      getChannels().updateChannels();
      new Query<>(OrgaTeam.class).col("team_name_created", nameCreation).update(id);
    }
  }

  public void setAbbreviationCreation(String abbreviationCreation) {
    final boolean updated = !this.abbreviationCreation.equals(abbreviationCreation);
    this.abbreviationCreation = abbreviationCreation;
    if (updated) {
      getChannels().updateChannels();
      new Query<>(OrgaTeam.class).col("team_abbr_created", nameCreation).update(id);
    }
  }

  public List<Membership> getActiveMemberships() {
    return new Query<>(Membership.class).where("orga_team", this).and("active", true).entityList();
  }

  public List<Membership> getMainMemberships() {
    return new Query<>(Membership.class).where("orga_team", this).and("active", true).and("role", TeamRole.MAIN)
        .entityList();
  }

  public Membership getMembership(TeamRole role, TeamPosition position) {
    return new Query<>(Membership.class).where("orga_team", this).and("active", true).and("role", role).and("position", position).entity();
  }

  public String getName() {
    return Util.avoidNull(getTeam(), nameCreation, AbstractTeam::getName);
  }

  public String getAbbreviation() {
    return Util.avoidNull(getTeam(), abbreviationCreation, AbstractTeam::getAbbreviation);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof OrgaTeam orgaTeam)) return false;
    if (teamId != null) return teamId.equals((orgaTeam.getTeamId()));
    return getId() == orgaTeam.getId();
  }

  public OrgaTeamScheduler getScheduler() {
    return new OrgaTeamScheduler(this);
  }

  public OrgaTeamChannelHandler getChannels() {
    return new OrgaTeamChannelHandler(this);
  }

  public OrgaTeamRoleHandler getRoleManager() {
    return new OrgaTeamRoleHandler(this);
  }

  @Override
  public int compareTo(@NotNull OrgaTeam o) {
    return Comparator.comparing(OrgaTeam::getPlace, Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(OrgaTeam::getId).compare(this, o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getNameCreation(), getAbbreviationCreation());
  }

  private AbstractTeam team;

  public AbstractTeam getTeam() {
    if (team == null) this.team = new Query<>(AbstractTeam.class).entity(teamId);
    return team;
  }

  public void setTeam(AbstractTeam team) {
    this.team = team;
    this.teamId = Util.avoidNull(team, AbstractTeam::getId);
    if (team != null) team.setOrgaTeam(this);
    new Query<>(OrgaTeam.class).col("team", team).update(id);
  }

  private CustomDiscordGroup group;

  public CustomDiscordGroup getGroup() {
    if (group == null) this.group = new Query<>(CustomDiscordGroup.class).entity(groupId);
    return group;
  }

  public List<Membership> getLeaders() {
    return getActiveMemberships().stream().filter(Membership::isCaptain).toList();
  }
}
