package de.xahrie.trues.api.scheduler;

import de.xahrie.trues.api.discord.util.Jinx;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ScheduleManager {
  public static Map<String, String> lastLines = new HashMap<>();

  public static void run() {
    new Timer().scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        Jinx.instance.getTasks().forEach(ScheduledTask::start);
      }
    }, 0, 60_000L);
  }
}
