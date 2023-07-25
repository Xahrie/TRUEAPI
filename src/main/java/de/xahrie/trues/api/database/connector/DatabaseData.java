package de.xahrie.trues.api.database.connector;

public record DatabaseData(String database, String password, int port, String server, String username) {
}
