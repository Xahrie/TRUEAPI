package de.xahrie.trues.api.logging;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

import de.xahrie.trues.api.database.connector.Listing;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.database.query.SQLEnum;
import de.xahrie.trues.api.discord.channel.AbstractDiscordChannel;
import de.xahrie.trues.api.discord.user.DiscordUser;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@Table(value = "orga_log", department = "message")
public class MessageLog extends AbstractServerLog implements Entity<MessageLog> {
  @Serial
  private static final long serialVersionUID = 5363213064192041859L;
  private final Integer channelId;
  private final MessageDeleteReason reason;

  public MessageLog(@NotNull LocalDateTime timestamp, @NotNull String details, @Nullable
  DiscordUser invoker, @Nullable DiscordUser target,
                    @NotNull
                    AbstractDiscordChannel channel, @NotNull MessageDeleteReason reason) {
    super(timestamp, details, invoker, target, ServerLogAction.MESSAGE_LOGGED);
    this.channel = channel;
    this.channelId = channel.getId();
    this.reason = reason;
  }

  public MessageLog(@Nullable DiscordUser invoker, @Nullable DiscordUser target, @NotNull String details,
                    @NotNull MessageDeleteReason reason) {
    super(LocalDateTime.now(), details, invoker, target, ServerLogAction.MESSAGE_LOGGED);
    this.channelId = null;
    this.reason = reason;
  }

  private MessageLog(int id, LocalDateTime timestamp, String details, Integer invokerId, Integer targetId, Integer channelId,
                     MessageDeleteReason reason) {
    super(id, timestamp, details, invokerId, targetId, ServerLogAction.MESSAGE_LOGGED);
    this.channelId = channelId;
    this.reason = reason;
  }

  public static MessageLog get(List<Object> objects) {
    return new MessageLog(
        (int) objects.get(0),
        (LocalDateTime) objects.get(2),
        (String) objects.get(5),
        (Integer) objects.get(3),
        (Integer) objects.get(4),
        (Integer) objects.get(8),
        new SQLEnum<>(MessageDeleteReason.class).of(objects.get(9))

    );
  }

  @Override
  public MessageLog create() {
    return new Query<>(MessageLog.class).key("log_time", getTimestamp()).key("invoker", getInvokerId()).key("target", getTargetId())
                                        .key("details", getDetails()).key("action", getAction()).col("channel", getChannel()).col("reason", reason)
                                        .insert(this);
  }

  private AbstractDiscordChannel channel;

  public AbstractDiscordChannel getChannel() {
    if (channel == null) this.channel = new Query<>(AbstractDiscordChannel.class).entity(channelId);
    return channel;
  }

  @Listing(Listing.ListingType.LOWER)
  public enum MessageDeleteReason {
    INSULT,
    BAD_BEHAVIOUR,
    OTHER
  }
}
