package de.xahrie.trues.api.database.connector;

import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.discord.LoadupManager;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.util.io.cfg.JSON;
import de.xahrie.trues.api.util.io.log.Console;
import de.xahrie.trues.api.util.io.log.DevInfo;
import lombok.Data;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;


public final class Database {
  private static DatabaseConnection connection;

  public static DatabaseConnection connection() {
    return connection(null);
  }

  public static DatabaseConnection connection(DatabaseData databaseData) {
    if (connection == null) {
      connection = run(databaseData);
      new Query<>(DiscordUser.class).col("joined", null).update(List.of());
    }
    return connection;
  }

  private static DatabaseConnection run(DatabaseData data) {
    Path folderPath = Paths.get("./resources/");
    Path configPath = Paths.get("./resources/connect.json");
    final URL resource = JSON.class.getResource("/connect.json");
    final DatabaseData databaseData =
        (Files.exists(folderPath) && Files.exists(configPath)) || resource != null ?
            readDatabaseDataFromJson(data) : data;

    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
      return new DatabaseConnection(databaseData, false);
    } catch (ClassNotFoundException | SQLException e) {
      new Console("SQL konnte nicht gefunden werden").severe(e);
      throw new RuntimeException(e);
    }
  }

  private static DatabaseData readDatabaseDataFromJson(DatabaseData databaseData) {
    try {
      final var json = JSON.read("connect.json");
      final JSONObject dbObject = json.getJSONObject("database");
      final String database = dbObject.getString("database");
      final String password = dbObject.getString("password");
      final int port = dbObject.getInt("port");
      final String server = dbObject.getString("server");
      final String username = dbObject.getString("username");
      return new DatabaseData(database, password, port, server, username);
    } catch (FileSystemNotFoundException ignored) {
      return databaseData;
    }
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

    public DatabaseConnection(DatabaseData databaseData, boolean commitMode) throws SQLException {
      this.connection = databaseData.getConnection(commitMode);
    }

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
