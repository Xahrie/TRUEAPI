package de.xahrie.trues.api.logging;

import java.time.LocalDateTime;

import de.xahrie.trues.api.database.connector.Table;

@Table("orga_log")
public interface AOrgaLog {
  String getDetails();
  LocalDateTime getTimestamp();
}
