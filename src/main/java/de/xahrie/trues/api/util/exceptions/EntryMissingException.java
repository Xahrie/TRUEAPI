package de.xahrie.trues.api.util.exceptions;

import java.util.NoSuchElementException;

import de.xahrie.trues.api.util.io.log.Console;
import de.xahrie.trues.api.util.io.log.DevInfo;
import de.xahrie.trues.api.util.io.log.Level;

/**
 * Thrown to indicate that the element being requested does not exist in the Database
 *
 * @see     java.util.Enumeration#nextElement()
 * @see     java.util.Iterator#next()
 * @since   1.0
 */
public class EntryMissingException extends NoSuchElementException {

  /**
   * Constructs a {@code EntryMissingException}, saving a reference
   * to the error message string {@code s} for later retrieval by the
   * {@code getMessage} method.
   *
   * @param   s   the detail message.
   */
  public EntryMissingException(String s) {
    super(s);
  }

  /**
   * Constructs a {@code EntryMissingException} with the specified detail
   * message and cause.
   *
   * @param s     the detail message, or null
   * @param cause the cause (which is saved for later retrieval by the
   *              {@link #getCause()} method), or null
   * @since 15
   */
  public EntryMissingException(String s, Throwable cause) {
    super(s, cause);
  }

  public EntryMissingException info() {
    return info(Level.WARNING);
  }

  public EntryMissingException info(Level level) {
    new DevInfo(super.getMessage()).with(Console.class).log(level, this);
    return this;
  }
}