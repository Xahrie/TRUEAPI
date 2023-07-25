package de.xahrie.trues.api.logging;

import de.xahrie.trues.api.database.connector.Listing;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.util.Util;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.channel.GenericChannelEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.override.GenericPermissionOverrideEvent;
import net.dv8tion.jda.api.events.guild.update.GenericGuildUpdateEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.role.GenericRoleEvent;
import net.dv8tion.jda.api.events.stage.GenericStageInstanceEvent;

import java.time.LocalDateTime;
import java.util.Arrays;

@Getter
@Table(value = "orga_log", department = "member")
public abstract class AbstractServerLog extends OrgaLog {
  private final Integer invokerId, targetId;
  private final ServerLogAction action;

  public AbstractServerLog(LocalDateTime timestamp, String details, DiscordUser invoker, DiscordUser target, ServerLogAction action) {
    super(timestamp, details);
    this.invoker = invoker;
    this.invokerId = Util.avoidNull(invoker, DiscordUser::getId);
    this.target = target;
    this.targetId = Util.avoidNull(target, DiscordUser::getId);
    this.action = action;
  }

  protected AbstractServerLog(int id, LocalDateTime timestamp, String details, Integer invokerId, Integer targetId, ServerLogAction action) {
    super(id, timestamp, details);
    this.invokerId = invokerId;
    this.targetId = targetId;
    this.action = action;
  }

  protected AbstractServerLog(DiscordUser invoker, DiscordUser target, String details, ServerLogAction action) {
    this(LocalDateTime.now(), details, invoker, target, action);
  }

  protected AbstractServerLog(DiscordUser target, String details, ServerLogAction action) {
    this(LocalDateTime.now(), details, null, target, action);
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

  @RequiredArgsConstructor
  @Getter
  @Listing(Listing.ListingType.LOWER)
  public enum ServerLogAction {
    APPLICATION_CREATED(null),
    OTHER(null),
    MINECRAFT(null),

    MESSAGE_DELETED(MessageDeleteEvent.class),
    MESSAGE_LOGGED(null),
    MESSAGE_UPDATED(MessageUpdateEvent.class),
    SERVER_JOIN(GuildMemberJoinEvent.class),
    SERVER_LEAVE(GuildMemberRemoveEvent.class),
    BAN(GuildBanEvent.class),
    UNBAN(GuildUnbanEvent.class),
    PERMISSION_CHANGE(GenericPermissionOverrideEvent.class),
    STAGE_CHANGE(GenericStageInstanceEvent.class),
    SERVER_CHANGE(GenericGuildUpdateEvent.class),
    CHANNEL_CHANGE(GenericChannelEvent.class),
    ROLE_CHANGE(GenericRoleEvent.class),
    COMMAND(GenericInteractionCreateEvent.class);
    private final Class<? extends GenericEvent> eventClass;

    public static ServerLogAction fromClass(Class<? extends GenericEvent> eventClass) {
      return Arrays.stream(ServerLogAction.values()).filter(action -> action.getEventClass() != null && action.getEventClass().equals(eventClass)).findFirst()
          .orElseGet(() -> Arrays.stream(ServerLogAction.values()).filter(action -> action.getEventClass() != null && action.getEventClass().isAssignableFrom(eventClass)).findFirst().orElse(OTHER));
    }
  }
}
