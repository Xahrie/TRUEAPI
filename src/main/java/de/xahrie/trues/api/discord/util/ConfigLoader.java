package de.xahrie.trues.api.discord.util;

import java.util.List;

import de.xahrie.trues.api.util.io.cfg.JSON;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public final class ConfigLoader {

  public static OnlineStatus getStatus() {
    final var json = JSON.read("apis.json");
    final var bot = json.getJSONObject("discord");
    final var status = bot.getString("status");
    return OnlineStatus.fromKey(status);
  }

  public static long getGuildId() {
    final var json = JSON.read("apis.json");
    final var bot = json.getJSONObject("discord");
    return bot.getLong("guild");
  }

  public static Activity getActivity() {
    final var json = JSON.read("apis.json");
    final var bot = json.getJSONObject("discord");
    final var activity = bot.getJSONObject("activity");
    return Activity.of(getActivityType(activity), activity.getString("text"));
  }

  public static List<GatewayIntent> getIntents() {
    final var json = JSON.read("apis.json");
    final var bot = json.getJSONObject("discord");
    final var intentsData = bot.getJSONArray("intents");
    return intentsData.toList().stream().map(String::valueOf).map(GatewayIntent::valueOf).toList();
  }

  @NotNull
  private static Activity.ActivityType getActivityType(JSONObject activity) {
    final var activityType = activity.getString("type");
    return Activity.ActivityType.valueOf(activityType);
  }
}
