package de.xahrie.trues.api.logging;

import java.time.LocalDateTime;

import de.xahrie.trues.api.database.query.Id;
import lombok.Getter;

@Getter
public abstract class OrgaLog implements AOrgaLog, Id {
  private int id;
  private final LocalDateTime timestamp;
  private final String details;

  public OrgaLog(LocalDateTime timestamp, String details) {
    this.timestamp = timestamp;
    this.details = details;
  }

  public OrgaLog(int id, LocalDateTime timestamp, String details) {
    this.id = id;
    this.timestamp = timestamp;
    this.details = details;
  }

  public void setId(int id) {
    this.id = id;
  }
}
