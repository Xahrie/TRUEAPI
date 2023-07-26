package de.xahrie.trues.api.database.connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public record DatabaseData(String database, String password, int port, String server, String username) {
  public Connection getConnection() throws SQLException {
    return getConnection(false);
  }

  public Connection getConnection(boolean commitMode) throws SQLException {
    final String url = "jdbc:mysql://" + server + ":" + port + "/" + database + "?sessionVariables=sql_mode=''";
    final Connection connection = DriverManager.getConnection(url, username, password);
    connection.setAutoCommit(commitMode);
    return connection;
  }
}
