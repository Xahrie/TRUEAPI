package de.xahrie.trues.api.discord.group;

import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.database.query.SQLEnum;
import de.xahrie.trues.api.discord.util.Jinx;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;

import java.io.Serial;
import java.util.List;

@Getter
@Table("discord_group")
public class CustomDiscordGroup implements Entity<CustomDiscordGroup> {
  @Serial
  private static final long serialVersionUID = 2036772268090427497L;

  @Setter
  private int id; // discord_group_id
  private final long discordId; // discord_id
  private String name; // role_name
  private final GroupType type; // role_type
  private final boolean fixed; // fixed

  public CustomDiscordGroup(long discordId, String name, GroupType type, boolean fixed) {
    this.discordId = discordId;
    this.name = name;
    this.type = type;
    this.fixed = fixed;
  }

  private CustomDiscordGroup(int id, long discordId, String name, GroupType type, boolean fixed) {
    this.id = id;
    this.discordId = discordId;
    this.name = name;
    this.type = type;
    this.fixed = fixed;
  }

  public static CustomDiscordGroup get(List<Object> objects) {
    return new CustomDiscordGroup(
        (int) objects.get(0),
        (long) objects.get(1),
        (String) objects.get(2),
        new SQLEnum<>(GroupType.class).of(objects.get(3)),
        (boolean) objects.get(4)
    );
  }

  @Override
  public CustomDiscordGroup create() {
    return new Query<>(CustomDiscordGroup.class).key("discord_id", discordId)
                                                .col("role_name", name).col("role_type", type).col("fixed", fixed)
                                                .insert(this);
  }

  public void setName(String name) {
    if (!this.name.equals(name)) new Query<>(CustomDiscordGroup.class).col("role_name", name).update(id);
    this.name = name;
  }

  public Role determineRole() {
    return Jinx.instance.getGuild().getRoleById(discordId);
  }

  public IPermissionContainer getChannel() {
    return (IPermissionContainer) Jinx.instance.getChannels().getChannel(discordId);
  }

  public void updatePermissions() {
    if (determineRole() != null) {
      determineRole().getManager().setPermissions(type.getPattern().getAllowed()).queue();
    }
  }

  private OrgaTeam orgaTeam;

  public OrgaTeam getOrgaTeam() {
    if (orgaTeam == null) this.orgaTeam = new Query<>(OrgaTeam.class).where("team_role", id).entity();
    return orgaTeam;
  }
}
