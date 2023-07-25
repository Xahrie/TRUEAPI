package de.xahrie.trues.api.logging;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.database.query.SQLEnum;
import de.xahrie.trues.api.util.io.log.Level;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(value = "orga_log", department = "custom")
public class CustomLog extends OrgaLog implements Entity<CustomLog> {
  @Serial
  private static final long serialVersionUID = -8523097440885813884L;

  private final Level level;

  public CustomLog(LocalDateTime timestamp, String details, Level level) {
    super(timestamp, details);
    this.level = level;
  }

  public CustomLog(int id, LocalDateTime timestamp, String details, Level level) {
    super(id, timestamp, details);
    this.level = level;
  }

  public static CustomLog get(List<Object> objects) {
    return new CustomLog(
        (int) objects.get(0),
        (LocalDateTime) objects.get(2),
        (String) objects.get(5),
        new SQLEnum<>(Level.class).of(objects.get(6))
    );
  }

  @Override
  public CustomLog create() {
    return new Query<>(CustomLog.class)
        .key("log_time", LocalDateTime.now()).key("details", getDetails()).key("action", level)
        .insert(this);
  }
}
