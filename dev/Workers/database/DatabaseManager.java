package dev.Workers.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Drops all user-created tables in the SQLite database.
     */
    public static void eraseDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Fetch all table names dynamically (ignoring internal sqlite tables)
            ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'");
            List<String> tables = new ArrayList<>();
            while (rs.next()) {
                tables.add(rs.getString("name"));
            }
            rs.close();

            // 2. Temporarily disable foreign keys to allow dropping tables in any order
            stmt.execute("PRAGMA foreign_keys = OFF");

            // 3. Drop all retrieved tables
            for (String table : tables) {
                stmt.execute("DROP TABLE IF EXISTS " + table);
            }

            // 4. Re-enable foreign keys
            stmt.execute("PRAGMA foreign_keys = ON");

        } catch (SQLException e) {
            System.err.println("Error erasing database: " + e.getMessage());
        }
    }
}