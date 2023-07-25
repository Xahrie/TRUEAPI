package de.xahrie.trues.api.database.connector;

import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.discord.LoadupManager;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.util.io.cfg.JSON;
import de.xahrie.trues.api.util.io.log.Console;
import de.xahrie.trues.api.util.io.log.DevInfo;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;


public final class Database {
  private static DatabaseConnection connection;

  public static DatabaseConnection connection() {
    if (connection == null) {
      connection = run();
      new Query<>(DiscordUser.class).col("joined", null).update(List.of());
    }
    return connection;
  }

  private static DatabaseConnection run()  {
    Path folderPath = Paths.get("./resources/");
    Path configPath = Paths.get("./resources/connect.json");
    final URL resource = JSON.class.getResource("/connect.json");
    final DatabaseData databaseData =
            (Files.exists(folderPath) && Files.exists(configPath)) || resource != null ?
                    readDatabaseDataFromJson() : readDatabaseDataFromYaml();

    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
      final String url = "jdbc:mysql://" + databaseData.server() + ":" + databaseData.port() + "/" +
                         databaseData.database() + "?sessionVariables=sql_mode=''";
      final Connection connection = DriverManager.getConnection(url, databaseData.username(), databaseData.password());
      connection.setAutoCommit(false);
      return new DatabaseConnection(connection);
    } catch (ClassNotFoundException | SQLException e) {
      new Console("SQL konnte nicht gefunden werden").severe(e);
      throw new RuntimeException(e);
    }
  }

  private static DatabaseData readDatabaseDataFromYaml() {
    ConfigurationSection configurationSection =
            Bukkit.getPluginManager().getPlugin("TRUEsports").getConfig()
                  .getConfigurationSection("database");
    if (configurationSection == null) throw new IllegalArgumentException("Daten sind leer");
    final String database = configurationSection.getString("database");
    final String password = configurationSection.getString("password");
    final int port = configurationSection.getInt("port");
    final String server = configurationSection.getString("server");
    final String username = configurationSection.getString("username");
    return new DatabaseData(database, password, port, server, username);
  }

  private static DatabaseData readDatabaseDataFromJson() {
    final var json = JSON.read("connect.json");
    final JSONObject dbObject = json.getJSONObject("database");
    final String database = dbObject.getString("database");
    final String password = dbObject.getString("password");
    final int port = dbObject.getInt("port");
    final String server = dbObject.getString("server");
    final String username = dbObject.getString("username");
    return new DatabaseData(database, password, port, server, username);
  }

  public static void disconnect() {
    connection.commit();
    try {
      connection.getConnection().close();
    } catch (SQLException e) {
      new DevInfo("SQL-Connection konnte nicht geschlossen werden").severe(e);
      throw new RuntimeException(e);
    }
    connection = null;
  }

  @Data
  public static class DatabaseConnection {
    private final Connection connection;
    private Boolean commitable = true;

    public Boolean isCloseable() {
      return commitable;
    }

    public void commit(@Nullable Boolean commitable) {
      if (commitable == null) {
        commit(true);
        this.commitable = false;
        return;
      }
      this.commitable = commitable;
      commit();
      LoadupManager.instance.askForDisconnect(null);
    }

    public void commit() {
      if (commitable) forceCommit();
    }

    public void forceCommit() {
      try {
        connection.commit();
      } catch (SQLException e) {
        try {
          connection.rollback();
        } catch (SQLException ex) {
          throw new RuntimeException(ex);
        }
        throw new RuntimeException(e);
      }
    }
  }
}
