package de.xahrie.trues.api.logging;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.database.query.SQLEnum;
import de.xahrie.trues.api.discord.user.DiscordUser;

@Table(value = "orga_log", department = "member")
public class ServerLog extends AbstractServerLog implements Entity<ServerLog> {
  @Serial private static final long serialVersionUID = 5363213064192041859L;

  public ServerLog(LocalDateTime timestamp, String details, DiscordUser invoker, DiscordUser target, ServerLogAction action) {
    super(timestamp, details, invoker, target, action);
  }

  public ServerLog(DiscordUser invoker, DiscordUser target, String details, ServerLogAction action) {
    this(LocalDateTime.now(), details, invoker, target, action);
  }

  public ServerLog(DiscordUser target, String details, ServerLogAction action) {
    this(LocalDateTime.now(), details, null, target, action);
  }

  private ServerLog(int id, LocalDateTime timestamp, String details, Integer invokerId, Integer targetId, ServerLogAction action) {
    super(id, timestamp, details, invokerId, targetId, action);
  }

  public static ServerLog get(List<Object> objects) {
    return new ServerLog(
        (int) objects.get(0),
        (LocalDateTime) objects.get(2),
        (String) objects.get(5),
        (Integer) objects.get(3),
        (Integer) objects.get(4),
        new SQLEnum<>(ServerLogAction.class).of(objects.get(6))
    );
  }

  @Override
  public ServerLog create() {
    return new Query<>(ServerLog.class).key("log_time", getTimestamp()).key("invoker", getInvokerId())
                                       .key("target", getTargetId()).key("details", getDetails()).key("action", getAction())
                                       .insert(this);
  }
}
