package de.xahrie.trues.api.scheduler;

import java.time.Duration;

import de.xahrie.trues.api.datatypes.calendar.TimeFormat;
import de.xahrie.trues.api.util.io.cfg.JSON;
import lombok.Getter;

public abstract class ScheduledTask {
  private final Schedule schedule;
  private Thread thread = null;
  @Getter
  private int loops = 0;
  @Getter
  private Duration duration = Duration.ZERO;

  public ScheduledTask() {
    this.schedule = getClass().asSubclass(this.getClass()).getAnnotation(Schedule.class);
  }

  public abstract void execute() throws InterruptedException;
  protected abstract String name();

  public void start() {
    if (thread != null && thread.isAlive() || notValid()) return;
    this.thread = new Thread(null, this::run, name());
    thread.start();
  }

  private boolean notValid() {
    return !new ScheduleComparer(schedule).test();
  }

  private void run() {
    try {
      final long start = System.currentTimeMillis();
      execute();
      final Duration duration = Duration.ofMillis(System.currentTimeMillis() - start);
      this.duration = this.duration.plus(duration);
      this.loops++;
      final String performanceData = getPerformanceData(duration);
      ScheduleManager.lastLines.put(name(), performanceData);
      JSON.append("thread-performance.txt", performanceData);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private String getPerformanceData(Duration duration) {
    final int average = (int) Math.round(this.duration.getSeconds() * 1. / loops);
    return "\n[" + TimeFormat.SYSTEM.now() + "] " + name() + " run " + duration.getSeconds() + " seconds (avg. " + average + ") - " + loops + "x";
  }
}
