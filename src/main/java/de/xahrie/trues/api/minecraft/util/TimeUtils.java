package de.xahrie.trues.api.minecraft.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

public final class TimeUtils {
  public static String differenceString(LocalDateTime start, LocalDateTime end) {
    return differenceString(start, end, false);
  }

  public static String differenceString(LocalDateTime start, LocalDateTime end, boolean fullDuration) {
    Duration between = Duration.between(start, end);
    return differenceString(between.getSeconds(), fullDuration);
  }

  public static String differenceString(long diffSeconds, boolean fullDuration) {
    final long diffInSeconds = Math.abs(diffSeconds);
    final long diffInMinutes = diffInSeconds / 60;
    final long diffInHours = diffInSeconds / 3600;
    final long diffInDays = diffInSeconds / 86400;
    final long diffInMonths = diffInDays / 30;
    String remainingSecs = diffInSeconds % 60 == 0 ? "00" : (diffInSeconds % 60 < 10 ? "0" : "") + diffInSeconds % 60;
    String remainingMins = diffInMinutes % 60 == 0 ? "00" : (diffInMinutes % 60 < 10 ? "0" : "") + diffInMinutes % 60;
    final String remainingHours = diffInHours % 24 == 0 ? "00" : (diffInHours % 24 < 10 ? "0" : "") + diffInHours % 24;
    final String remainingDays = (diffInDays < 10 ? "0" : "") + diffInDays;
    if (fullDuration) {
      if (diffInSeconds < 60) {
        return ":" + remainingSecs;
      }
      if (diffInMinutes < 60) {
        return remainingMins + ":" + remainingSecs;
      }
      if (diffInHours < 24) {
        return remainingHours + ":" + remainingMins;
      }
      return remainingDays + ":" + remainingHours + ":" + remainingMins;
    }

    remainingSecs = diffInSeconds % 60 == 0 ? "" : ":" + (diffInSeconds % 60 < 10 ? "0" : "") + diffInSeconds % 60;
    remainingMins = diffInMinutes % 60 == 0 ? "" : ":" + (diffInMinutes % 60 < 10 ? "0" : "") + diffInMinutes % 60;
    final String output = diffSeconds > 0 ? "vor " : "in ";

    if (diffInSeconds < 100) {
      return output + diffInSeconds + " Sekunden";
    }

    if (diffInMinutes < 10) {
      return output + diffInMinutes + remainingSecs + " Minuten";
    }
    if (diffInMinutes < 100) {
      return output + diffInMinutes + " Minuten";
    }

    if (diffInHours < 10) {
      return output + diffInHours + remainingMins + " Stunden";
    }
    if (diffInHours < 100) {
      return output + diffInHours + " Stunden";
    }

    if (diffInDays < 14) {
      return output + diffInDays + " Tage";
    }
    if (diffInDays < 60) {
      final long diffInWeeks = diffInDays / 7;
      return output + diffInWeeks + " Wochen";
    }
    if (diffInDays < 360) {
      return output + diffInMonths + " Monate";
    }
    final long diffInYears = diffInDays / 360;
    return output + diffInYears + " Jahre" + (diffInMonths % 12 == 0 ? "" : " " + diffInMonths % 12 + " Monate");
  }

  public static int get(Date date, int calendarId) {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    return calendar.get(calendarId);
  }
}
