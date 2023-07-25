package de.xahrie.trues.api.discord.builder.leaderboard;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.xahrie.trues.api.util.io.cfg.JSON;
import org.json.JSONArray;
import org.json.JSONObject;

public class LeaderboardHandler {
  private static final Map<PublicLeaderboard, LocalDateTime> leaderboards;

  static {
    final LocalDateTime dateTime = LocalDate.of(2000, Month.JANUARY, 1).atTime(LocalTime.MIN);
    leaderboards = load().stream().collect(HashMap::new, (m, v) -> m.put(v, dateTime), HashMap::putAll);
  }

  private static List<PublicLeaderboard> load() {
    final JSONArray dataArray = JSON.read("leaderboards.json").getJSONArray("data");
    return IntStream.range(0, dataArray.length()).mapToObj(dataArray::getJSONObject).map(PublicLeaderboard::fromJSON).collect(Collectors.toList());
  }

  public static void handleLeaderboards() {
    final LocalDateTime dateTime = LocalDateTime.now();
    leaderboards.forEach((leaderboard, last) -> {
      final int frequency = leaderboard.getCustomQuery().getFrequencyInMinutes();
      if (frequency == 0) return;
      if (Duration.between(last, dateTime).get(ChronoUnit.SECONDS) / 60 >= frequency - 1) {
        leaderboard.updateData();
        leaderboards.replace(leaderboard, LocalDateTime.now());
      }
    });
  }

  public static String add(PublicLeaderboard leaderboard) {
    return add(leaderboard, 0);
  }

  private static String add(PublicLeaderboard leaderboard, int iteration) {
    if (leaderboard.required != 0 && leaderboard.required == leaderboard.getMessageIds().size()) {
      leaderboards.put(leaderboard, LocalDateTime.now());
      write();
      return "Das Leaderboard konnte erfolgreich gespeichert werden";
    }

    if (iteration == 30*55) return "Das Leaderboard konnte nicht gespeichert werden und wird sich nicht aktualisieren.";

    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    return add(leaderboard, iteration + 1);
  }

  private static void write() {
    final var leaderboardContent = new JSONObject();
    final var data = new JSONArray(leaderboards.keySet().stream().map(PublicLeaderboard::toJSON).toList());
    leaderboardContent.put("data", data);
    JSON.write("leaderboards.json", leaderboardContent.toString());
  }
}
