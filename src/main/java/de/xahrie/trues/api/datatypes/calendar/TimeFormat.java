package de.xahrie.trues.api.datatypes.calendar;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import de.xahrie.trues.api.util.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.ExtensionMethod;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;

/**
 * <table class="striped">
 * <caption>some Pattern Letters and Symbols</caption>
 * <thead>
 *  <tr><th scope="col">Symbol</th>   <th scope="col">Meaning</th>         <th scope="col">Presentation</th> <th scope="col">Examples</th>
 * </thead>
 * <tbody>
 *   <tr><th scope="row">y/u</th>     <td>year-of-era</td>                 <td>year</td>              <td>2004; 04</td>
 *   <tr><th scope="row">D</th>       <td>day-of-year</td>                 <td>number</td>            <td>189</td>
 *   <tr><th scope="row">M/L</th>     <td>month-of-year</td>               <td>number/text</td>       <td>7; 07; Jul; July; J</td>
 *   <tr><th scope="row">d</th>       <td>day-of-month</td>                <td>number</td>            <td>10</td>
 *   <tr>
 *   <tr><th scope="row">w</th>       <td>week-of-week-based-year</td>     <td>number</td>            <td>27</td>
 *   <tr><th scope="row">W</th>       <td>week-of-month</td>               <td>number</td>            <td>4</td>
 *   <tr><th scope="row">E</th>       <td>day-of-week</td>                 <td>text</td>              <td>Tue; Tuesday; T</td>
 *   <tr><th scope="row">e/c</th>     <td>localized day-of-week</td>       <td>number/text</td>       <td>2; 02; Tue; Tuesday; T</td>
 *   <tr>
 *   <tr><th scope="row">H</th>       <td>hour-of-day (0-23)</td>          <td>number</td>            <td>0</td>
 *   <tr><th scope="row">m</th>       <td>minute-of-hour</td>              <td>number</td>            <td>30</td>
 *   <tr><th scope="row">s</th>       <td>second-of-minute</td>            <td>number</td>            <td>55</td>
 *   <tr><th scope="row">S</th>       <td>fraction-of-second</td>          <td>fraction</td>          <td>978</td>
 *   <tr><th scope="row">A</th>       <td>milli-of-day</td>                <td>number</td>            <td>1234</td>
 *   <tr>
 *   <tr><th scope="row">O</th>       <td>localized zone-offset</td>       <td>offset-O</td>          <td>GMT+8; GMT+08:00; UTC-08:00</td>
 *   <tr><th scope="row">x</th>       <td>zone-offset</td>                 <td>offset-x</td>          <td>+0000; -08; -0830; -08:30; -083015; -08:30:15</td>
 *   <tr><th scope="row">Z</th>       <td>zone-offset</td>                 <td>offset-Z</td>          <td>+0000; -0800; -08:00</td>
 * </tbody>
 * </table>
 */
@Getter
@RequiredArgsConstructor
@ToString
@Log
@ExtensionMethod(StringUtils.class)
public enum TimeFormat {
  AUTO("null", ""),
  DAY("dd.MM.", ""),
  HOUR_SHORT("HH", ""),
  HOUR("HH:mm", ""),
  DAY_LONG("dd.MM.YYYY", ""),
  DAY_STANDARD("YYYY-MM-dd", ""),
  DISCORD("<t:1234567890:R>", ""),
  DEFAULT_FULL("E, d. MMM YYYY HH:mm", ""),
  DEFAULT_SHORT("E, HH:mm", ""),
  DEFAULT("E, d. MMM HH:mm", ""),
  DEFAULT_DAY("E, d. MMM", ""),
  SYSTEM("YYYY-MM-dd HH:mm:ss", ""),
  WEEKLY("EE., HH", " Uhr");

  private final String format;
  private final String suffix;

  public String of(LocalDate date) {
    return of(LocalDateTime.of(date, LocalTime.MIN));
  }

  public String of(LocalTime time) {
    return of(LocalDateTime.of(LocalDate.now(), time));
  }

  public String of(LocalDateTime time) {
    if (equals(AUTO)) return handleAuto(time);
    if (equals(DISCORD)) return "<t:" + time.atZone(ZoneId.systemDefault()).toEpochSecond() + ":R>";
    return time.format(DateTimeFormatter.ofPattern(format)) + suffix;
  }

  public LocalDateTime of(String text) {
    if (equals(AUTO)) throw new IllegalArgumentException("AUTO nicht zulässig.");
    if (equals(DISCORD)) return DateTimeUtils.fromEpoch(text.between("<t:", ":R>").intValue());
    return LocalDateTime.parse(text, DateTimeFormatter.ofPattern(format));
  }

  @NotNull
  public String now() {
    return of(LocalDateTime.now());
  }

  /**
   * weniger als 45 Minuten -> DISCORD <br>
   * heute -> <b>HH:MM Uhr</b> <br>
   * weniger als 24 Stunden -> DISCORD <br>
   * weniger als 7 Tage -> <b>Mo, HH:MM Uhr</b> <br>
   * weniger als 25 Tage -> DISCORD <br>
   * sonst -> <b>Mo, DD. MMM HH:MM Uhr</b>
   */
  private String handleAuto(LocalDateTime time) {
    final Duration duration = Duration.between(time, LocalDateTime.now());
    final long seconds = Math.abs(duration.getSeconds());
    if (seconds < 45 * 60) return DISCORD.of(time);
    else if (time.toLocalDate().equals(LocalDate.now())) return HOUR.of(time) + " Uhr";
    else if (seconds < 24 * 60 * 60) return DISCORD.of(time);
    else if (seconds < 7 * 24 * 60 * 60) return DEFAULT_SHORT.of(time) + " Uhr";
    else if (seconds < 25 * 24 * 60 * 60) return DISCORD.of(time);
    else return DEFAULT.of(time) + " Uhr";
  }
}
