package de.xahrie.trues.api.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.xahrie.trues.api.calendar.scheduling.DateTimeStringConverter;
import de.xahrie.trues.api.datatypes.calendar.DateTimeUtils;
import de.xahrie.trues.api.util.io.log.DevInfo;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.helpers.MessageFormatter;

public class StringUtils {

  public static String replaces(@NonNull String value, @NonNull String sequence, int index) {
    return value.substring(0, index) + sequence + value.substring(index + sequence.length());
  }
  public static String upper(String value) {
    return value.toUpperCase();
  }

  public static <T extends Enum<T>> T toEnum(String value, Class<T> clazz) {
    return toEnum(value, clazz, null);
  }

  public static <T extends Enum<T>> T toEnum(String value, Class<T> clazz, T alternative) {
    final String replace = upper(value).replace(" ", "_");
    try {
      return Enum.valueOf(clazz, replace);
    } catch (IllegalArgumentException ignored) {
      return alternative;
    }
  }

  /**
   * @param start Wenn <code>start = null oder nicht in value</code>, dann startindex immer 0
   * @param end Wenn <code>end = null oder nicht in value</code>, dann max length
   * @return Sequenz zwischen entsprechenden Werten
   */
  public static String between(@NonNull String value, @Nullable String start, @NonNull String end) {
    return between(value, start, end, 1);
  }

  public static String before(@NonNull String value, @NonNull String end) {
    return before(value, end, 1);
  }


  public static String before(@NonNull String value, @NonNull String end, int occurrence) {
    return between(value, null, end, occurrence);
  }

  /**
   * @param start Wenn <code>start = null oder nicht in value</code>, dann startindex immer 0
   * @return Sequenz zwischen entsprechenden Werten
   */
  public static String after(@NonNull String value, @NonNull String start) {
    return after(value, start, 1);
  }

  /**
   * @param start Wenn <code>start = null oder nicht in value</code>, dann startindex immer 0
   * @return Sequenz zwischen entsprechenden Werten
   */
  public static String after(@NonNull String value, @NonNull String start, int occurrence) {
    return between(value, start, null, occurrence);
  }

  /**
   * @param start Wenn <code>start = null oder nicht in value</code>, dann startindex immer 0
   * @param end Wenn <code>end = null oder nicht in value</code>, dann max length
   * @return Sequenz zwischen entsprechenden Werten
   */
  public static String between(@NonNull String value, @Nullable String start, @Nullable String end, int occurrence) {
    int startIndex = -1;
    int endIndex = value.length();
    if (start != null) {
      startIndex = ordinalIndexOf(value, start, occurrence) + start.length() - 1;
      if (end != null)
        endIndex = value.indexOf(end, startIndex + 1);
    } else if (end != null)
      endIndex = ordinalIndexOf(value, end, occurrence);

    if (endIndex == -1)
      endIndex = value.length();

    if (endIndex < startIndex) {
      final RuntimeException exception = new IndexOutOfBoundsException("Index-Fehler");
      new DevInfo().severe(exception);
      throw exception;
    }

    return value.substring(startIndex + 1, endIndex);
  }

  /**
   * @return Index, wo der String zum {@code n}-ten Mal auftritt. <br>
   * Wenn nicht vorhanden, dann {@code -1}
   */
  public static int ordinalIndexOf(String value, String key, int ordinal) {
    int pos = value.lastIndexOf(key);
    if (ordinal < 0) {
      ordinal = Math.abs(ordinal);
      while (--ordinal > 0 && pos != -1) pos = value.lastIndexOf(key, pos - 1);
    } else {
      pos = value.indexOf(key);
      while (--ordinal > 0 && pos != -1) pos = value.indexOf(key, pos + 1);
    }
    return pos;
  }

  /**
   * @return -1 if not a number
   */
  public static int intValue(String value) {
    return intValue(value, -1);
  }

  public static Integer intValue(String value, Integer defaultValue) {
    if (value == null) return defaultValue;
    final Double aDouble = doubleValue(value, Util.avoidNull(defaultValue, null, Double::valueOf));
    return Util.avoidNull(aDouble, Double::intValue);
  }


  /**
   * @return -1 if not a number
   */
  public static long longValue(String value) {
    return longValue(value, -1);
  }

  public static Long longValue(String value, Integer defaultValue) {
    final Double aDouble = doubleValue(value, Util.avoidNull(defaultValue, null, Double::valueOf));
    return Util.avoidNull(aDouble, Double::longValue);
  }

  /**
   * @return -1 if not a number
   */
  public static double doubleValue(String value) {
    return doubleValue(value, -1.);
  }

  public static Double doubleValue(String value, Double defaultValue) {
    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException ignored) {  }
    return defaultValue;
  }

  public static LocalDateTime getDateTime(String value) {
    final LocalDateTime localDateTime = new DateTimeStringConverter(value).toTime();
    if (localDateTime == null) return null;

    return DateTimeUtils.fromEpoch((int) localDateTime.toEpochSecond(ZoneOffset.ofHours(2)));
  }

  public static int countMatches(String value, String sub) {
    if (value.isEmpty() || sub.isEmpty()) return 0;
    int count = 0;
    int idx = 0;
    while ((idx = value.indexOf(sub, idx)) != -1) {
      count++;
      idx += sub.length();
    }
    return count;
  }

  public static String keep(String value, int maxLength) {
    return value.substring(0, Math.min(maxLength, value.length()));
  }

  public static String erase(String value, int eraseAmount) {
    if (eraseAmount > value.length()) return value;
    if (eraseAmount >= 0) return value.substring(eraseAmount);
    return value.substring(0, value.length() - eraseAmount);
  }

  public static String concat(String value, int start, int end) {
    if (end > value.length()) return value.substring(start);
    if (start > value.length()) return "";
    return value.substring(start, end);
  }

  public static String capitalizeFirst(String value) {
    return value.isBlank() ? "" : value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
  }

  public static String capitalizeEnum(String value) {
    return Arrays.stream(value.replace("_", " ").split(" "))
        .map(StringUtils::capitalizeFirst).collect(Collectors.joining(" "));
  }

  public static int count(String value, String substring) {
    int count = 0, fromIndex = 0;
    while ((fromIndex = value.indexOf(substring, fromIndex)) != -1) {
      count++;
      fromIndex++;
    }
    return count;
  }

  public static String format(String value, Object... data) {
    return format(value, 0, data);
  }

  public static String format(String value, int startingIndex, Object... data) {
    if (data.length == 0) return value;

    final List<Object> objects = Arrays.stream(data).toList().subList(startingIndex, count(value, "{}"));
    return MessageFormatter.arrayFormat(value, objects.toArray()).getMessage();
  }

}
