package de.xahrie.trues.api.util.io.cfg;

public class LogFiles {
  public static void log(Exception exception) {
    FileLog.getInstance().logger().throwing("???", "???", exception);
  }

  public static void log(String output) {
    FileLog.getInstance().logger().warning(output);
  }
}
