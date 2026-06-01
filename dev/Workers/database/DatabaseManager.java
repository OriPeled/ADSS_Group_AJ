package dev.Workers.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:superli.db";

    private DatabaseManager() {
    }

    public static Connection getConnection() throws SQLException {
        loadSqliteDriver();

        Connection connection = DriverManager.getConnection(DB_URL);
        enableForeignKeys(connection);

        return connection;
    }

    private static void loadSqliteDriver() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "SQLite JDBC driver was not found. Make sure sqlite-jdbc.jar is added to the project libraries.",
                    e
            );
        }
    }

    private static void enableForeignKeys(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
    }
}