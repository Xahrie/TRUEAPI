package de.xahrie.trues.api.minecraft.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.xahrie.trues.api.util.Util;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Getter
public class UUIDFetcher {
  private static final Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();
  private static final Map<String, UUID> uuidCache = new HashMap<>();
  private static final Map<UUID, String> nameCache = new HashMap<>();
  private static final ExecutorService pool = Executors.newCachedThreadPool();

  private String name;
  private UUID id;

  /**
   * Fetches the uuid asynchronously and passes it to the consumer
   *
   * @param name The name
   * @param action Do what you want to do with the uuid her
   */
  public static void getUUID(String name, Consumer<UUID> action) {
    pool.execute(() -> action.accept(getUUID(name)));
  }

  /**
   * Fetches the uuid synchronously and returns it
   *
   * @param name The name
   * @return The uuid
   */
  public static UUID getUUID(String name) {
    name = name.toLowerCase();
    if (uuidCache.containsKey(name)) return uuidCache.get(name);

    try {
      final URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
      final UUIDFetcher fetcher = getUUIDFetcher(url);
      return Util.avoidNull(fetcher, UUIDFetcher::getId);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Fetches the name asynchronously and passes it to the consumer
   *
   * @param uuid The uuid
   * @param action Do what you want to do with the name her
   */
  public static void getName(UUID uuid, Consumer<String> action) {
    pool.execute(() -> action.accept(getName(uuid)));
  }

  /**
   * Fetches the name synchronously and returns it
   *
   * @param uuid The uuid
   * @return The name
   */
  @Nullable
  public static String getName(UUID uuid) {
    if (nameCache.containsKey(uuid)) return nameCache.get(uuid);

    try {
      final URL url = new URL("https://api.mojang.com/user/profile/" + UUIDTypeAdapter.fromUUID(uuid));
      final UUIDFetcher fetcher = getUUIDFetcher(url);
      return Util.avoidNull(fetcher, UUIDFetcher::getName);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Nullable
  private static UUIDFetcher getUUIDFetcher(@NotNull URL url) {
    try {
      final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setReadTimeout(5000);
      final InputStreamReader input = new InputStreamReader(connection.getInputStream());
      final UUIDFetcher uuidFetcher = gson.fromJson(new BufferedReader(input), UUIDFetcher.class);
      if (uuidFetcher != null) {
        uuidCache.put(uuidFetcher.name.toLowerCase(), uuidFetcher.id);
        nameCache.put(uuidFetcher.id, uuidFetcher.name);
      }
      return uuidFetcher;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}