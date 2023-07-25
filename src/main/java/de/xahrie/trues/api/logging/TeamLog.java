package de.xahrie.trues.api.logging;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.database.connector.Listing;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.database.query.SQLEnum;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.util.Util;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Table(value = "orga_log", department = "team")
public class TeamLog extends OrgaLog implements Entity<TeamLog> {
  @Serial private static final long serialVersionUID = 7425349836183090767L;
  private final Integer invokerId;
  private final TeamLogAction action;
  private final int targetId, teamId;

  public TeamLog(@Nullable
  DiscordUser invoker, @NotNull DiscordUser target, @NotNull String details, @NotNull TeamLogAction action,
                 @NotNull
                 OrgaTeam team) {
    this(LocalDateTime.now(), details, invoker, target, action, team);
  }

  public TeamLog(@NotNull LocalDateTime timestamp, @NotNull String details, @Nullable DiscordUser invoker, @NotNull DiscordUser target,
                 @NotNull TeamLogAction action, @NotNull OrgaTeam team) {
    super(timestamp, details);
    this.invoker = invoker;
    this.invokerId = Util.avoidNull(invoker, DiscordUser::getId);
    this.target = target;
    this.targetId = target.getId();
    this.action = action;
    this.team = team;
    this.teamId = team.getId();
  }

  private TeamLog(int id, LocalDateTime timestamp, String details, Integer invokerId, int targetId, TeamLogAction action, int teamId) {
    super(id, timestamp, details);
    this.invokerId = invokerId;
    this.targetId = targetId;
    this.action = action;
    this.teamId = teamId;
  }

  public static TeamLog get(List<Object> objects) {
    return new TeamLog(
        (int) objects.get(0),
        (LocalDateTime) objects.get(2),
        (String) objects.get(5),
        (Integer) objects.get(3),
        (int) objects.get(4),
        new SQLEnum<>(TeamLogAction.class).of(objects.get(6)),
        (int) objects.get(7)
    );
  }

  @Override
  public TeamLog create() {
    return new Query<>(TeamLog.class).key("log_time", getTimestamp()).key("invoker", invokerId)
                                     .key("target", targetId).key("details", getDetails()).key("action", action).key("team", teamId)
                                     .insert(this);
  }

  private DiscordUser invoker;

  public DiscordUser getInvoker() {
    if (invoker == null) this.invoker = new Query<>(DiscordUser.class).entity(invokerId);
    return invoker;
  }

  private DiscordUser target;

  public DiscordUser getTarget() {
    if (target == null) this.target = new Query<>(DiscordUser.class).entity(targetId);
    return target;
  }

  private OrgaTeam team;

  public OrgaTeam getTeam() {
    if (team == null) this.team = new Query<>(OrgaTeam.class).entity(teamId);
    return team;
  }

  @Listing(Listing.ListingType.LOWER)
  public enum TeamLogAction {
    LINEUP_JOIN,
    LINEUP_LEAVE
  }
}
