package de.xahrie.trues.api.util.io.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

import de.xahrie.trues.api.discord.user.DiscordUser;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class AbstractLog<T extends AbstractLog<T>> {
  protected String message = null;
  protected Throwable throwable = null;
  protected Level level = null;
  protected final List<AbstractLog<?>> otherLoggers = new ArrayList<>();

  public AbstractLog(String message) {
    this.message = message;
  }

  public T msg(String message) {
    this.message = message;
    return (T) this;
  }

  public String msg() {
    return message;
  }

  public T exception(Throwable throwable) {
    this.throwable = throwable;
    return (T) this;
  }

  public Throwable exception() {
    return throwable;
  }

  /**
   * weitere Logs werden jetzt mit Loggerklasse gecastet
   * @return Instanz fuer Chaining
   */
  public T with(Class<? extends AbstractLog<?>> logger) {
    try {
      final AbstractLog<?> abstractLog = logger.getConstructor(String.class).newInstance(message);
      otherLoggers.add(abstractLog);
      return (T) this;
    } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  public abstract T doCommand(DiscordUser user, String command, String full);
  protected abstract T doLog();

  private T log(Level level) {
    return log(level, null);
  }

  private T log(Level level, Throwable throwable) {
    this.level = level;
    this.throwable = throwable;
    otherLoggers.add(this);
    otherLoggers.forEach(abstractLog -> abstractLog.level = level);
    otherLoggers.forEach(AbstractLog::doLog);
    return (T) this;
  }

  public T severe() {
    return log(Level.SEVERE);
  }

  public T severe(Throwable throwable) {
    return log(Level.SEVERE, throwable);
  }

  public T error() {
    return log(Level.ERROR);
  }

  public T error(Throwable throwable) {
    return log(Level.ERROR, throwable);
  }

  public T warn() {
    return log(Level.WARNING);
  }

  public T warn(Throwable throwable) {
    return log(Level.WARNING, throwable);
  }

  public T debug() {
    return log(Level.DEBUG);
  }

  public T debug(Throwable throwable) {
    return log(Level.DEBUG, throwable);
  }

  public T config() {
    return log(Level.CONFIG);
  }

  public T config(Throwable throwable) {
    return log(Level.CONFIG, throwable);
  }

  public T entering() {
    this.message = "BEGINN VON (" + message + ")";
    return debug();
  }

  public T exiting() {
    this.message = "ENDE VON (" + message + ")";
    return debug();
  }

  public T info() {
    return log(Level.INFO);
  }

  public T info(Throwable throwable) {
    return log(Level.INFO, throwable);
  }

  public String getStackTrace() {
    return throwable == null ? "" : getThrowWriter().toString();
  }

  private StringWriter getThrowWriter() {
    final StringWriter sw = new StringWriter();
    final PrintWriter pw = new PrintWriter(sw);
    throwable.printStackTrace(pw);
    return sw;
  }

  @Override
  public String toString() {
    final String messageFormatted;
    if (this instanceof DevInfo) {
      messageFormatted = "[" + level.name() + "] **" + message.replace("**", "__") + "**";
    } else if (this instanceof SQLInfo) {
      messageFormatted = message.replace("**", "__");
    } else {
      messageFormatted = LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)) +
          " [" + level.name() + "] " + message.replace("**", "").replace("__", "");
    }
    final String stackTrace = getStackTrace();
    final boolean split = !messageFormatted.isBlank() && !stackTrace.isBlank();
    return Thread.currentThread().getName() + " -> " + messageFormatted + (split ? "\n" : "") + stackTrace;
  }
}
