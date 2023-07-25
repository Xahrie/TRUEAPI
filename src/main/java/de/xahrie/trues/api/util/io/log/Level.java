package de.xahrie.trues.api.util.io.log;

import java.util.List;

import de.xahrie.trues.api.database.connector.Listing;

@Listing(Listing.ListingType.LOWER)
public enum Level {
  /**
   * SEVERE is a message level indicating a serious failure.
   * <p>
   * In general SEVERE messages should describe events that are
   * of considerable importance and which will prevent normal
   * program execution.   They should be reasonably intelligible
   * to end users and to system administrators.
   * This level is initialized to <CODE>1000</CODE>.
   */
  SEVERE,
  /**
   * FINE is a message level providing tracing information. <p>
   * broadly interesting to developers
   * FINE messages might include things like minor (recoverable) failures. Issues indicating potential performance problems
   * are also worth logging as FINE.
   */
  ERROR,
  /**
   * WARNING is a message level indicating a potential problem.
   */
  WARNING,
  /**
   * INFO is a message level for informational messages.
   */
  INFO,
  COMMAND,
  /**
   * CONFIG is a message level for static configuration messages.
   */
  CONFIG,
  /**
   * FINER indicates a fairly detailed tracing message. <p>
   * By default logging calls for entering, returning, or throwing an exception are traced at this level.
   */
  DEBUG;

  public static final List<Level> CONSOLE_LOG = List.of(SEVERE, ERROR, WARNING, INFO, CONFIG, DEBUG);
  public static final List<Level> DISCORD_LOG = List.of(SEVERE, ERROR, WARNING, INFO, COMMAND, CONFIG);
  public static final List<Level> DATABASE_LOG = List.of(COMMAND);
}
