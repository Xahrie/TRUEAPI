package de.xahrie.trues.api.discord;

import de.xahrie.trues.api.Registerer;
import de.xahrie.trues.api.coverage.team.TeamLoader;
import de.xahrie.trues.api.database.connector.Database;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.discord.builder.modal.ModalImpl;
import de.xahrie.trues.api.discord.command.context.ContextCommand;
import de.xahrie.trues.api.discord.command.slash.SlashCommand;
import de.xahrie.trues.api.discord.util.Jinx;
import de.xahrie.trues.api.scheduler.ScheduledTask;
import de.xahrie.trues.api.scouting.AnalyzeManager;
import de.xahrie.trues.api.scouting.analyze.RiotPlayerAnalyzer;
import de.xahrie.trues.api.util.Connectable;
import de.xahrie.trues.api.util.Const;
import de.xahrie.trues.api.util.io.log.DevInfo;
import lombok.extern.java.Log;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.TimeZone;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

@Log
public abstract class LoadupManager implements Connectable {
  public static LoadupManager instance;
  protected Long disconnectingMillis = null;
  protected boolean restartAfter;
  protected long initMillis;

  protected LoadupManager() {
    this.initMillis = System.currentTimeMillis();
  }

  /**
   * ueberpruefe Connection
   */
  @Override
  public void connect() {
    if (!Const.check()) System.exit(1);

    TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
    final Handler consoleHandler = new ConsoleHandler();
    consoleHandler.setLevel(Level.FINE);
    Logger.getAnonymousLogger().addHandler(consoleHandler);
    register();
    Jinx.instance.connect();
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
      e.printStackTrace();
      new DevInfo().warn(e);
    });
    instance = this;
    log.info("System gestartet in " + (System.currentTimeMillis() - initMillis) + " Millisekunden.");
  }

  public LoadupManager register(Registerer<SlashCommand> commands, Registerer<ContextCommand> context,
          Registerer<ListenerAdapter> events, Registerer<ModalImpl> modals,
          Registerer<ScheduledTask> tasks) {
    Jinx.instance = new Jinx(commands, context, events, modals, tasks);
    return this;
  }

  public abstract void register();

  @Override
  public void disconnect() {
    this.disconnectingMillis = System.currentTimeMillis();
    askForDisconnect(false);
  }

  public void restart() {
    this.disconnectingMillis = System.currentTimeMillis();
    askForDisconnect(true);
    reset();
  }

  private void reset() {
    AnalyzeManager.reset();
    RiotPlayerAnalyzer.reset();
    TeamLoader.reset();
    Query.reset();
    Jinx.instance.resetTasks();
  }

  public void askForDisconnect(Boolean restart) {
    if (restart != null) this.restartAfter = restart;
    if (disconnectingMillis != null && Database.connection().isCloseable()) {
      doDisconnect();
    }
  }

  private void doDisconnect() {
    Jinx.instance.disconnect();
    Database.disconnect();
    instance = null;
    log.info("System beendet in " + (System.currentTimeMillis() - disconnectingMillis) + " Millisekunden.");
    handleRestart();
  }

  private void handleRestart() {
    if (restartAfter) {
      this.disconnectingMillis = null;
      this.initMillis = System.currentTimeMillis();
      connect();
    }
  }
}
