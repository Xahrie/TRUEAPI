package de.xahrie.trues.api.util.io.cfg;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import de.xahrie.trues.api.datatypes.calendar.TimeFormat;
import de.xahrie.trues.api.util.Const;

public record FileLog(Logger logger, LocalDate date) {
  private static FileLog instance;

  public static FileLog getInstance() {
    if (instance == null || instance.date.isBefore(LocalDate.now())) {
      instance = createFileLog();
    }
    return instance;
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  private static FileLog createFileLog() {
    final Logger l = Logger.getLogger("FileLog");
    l.setLevel(Level.FINER);
    final LocalDate today = LocalDate.now();
    final String todayStr = TimeFormat.DAY_STANDARD.of(today);

    try {
      final Path currentRelativePath = Paths.get("");
      final File directory = new File(currentRelativePath.toAbsolutePath() + "/logs");
      directory.mkdir();
      final File file = new File(currentRelativePath.toAbsolutePath() + "/logs/" + todayStr + ".log");
      file.createNewFile();
      final FileHandler handler = new FileHandler(file.getPath(), Const.SAVE_LOGS);
      handler.setFormatter(new SimpleFormatter());
      l.addHandler(handler);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return new FileLog(l, today);
  }
}
