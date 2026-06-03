package dev.Workers.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void initializeDatabase() {
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement()) {

            createEmployeesTables(statement);
            createShiftsTables(statement);
            createMockTransportTables(statement);

            System.out.println("Database tables initialized successfully.");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database.", e);
        }
    }

    private static void createEmployeesTables(Statement statement) throws SQLException {

        statement.execute("""
                CREATE TABLE IF NOT EXISTS employees (
                    employee_id TEXT PRIMARY KEY,
                    first_name TEXT NOT NULL,
                    last_name TEXT NOT NULL,
                    bank_account TEXT,
                    salary REAL,
                    employment_terms TEXT,
                    start_date TEXT NOT NULL,
                    is_active INTEGER NOT NULL DEFAULT 1,
                    driver_license_type TEXT
                );
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS access_credentials (
                    employee_id TEXT PRIMARY KEY,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    permission_level TEXT NOT NULL,
                    FOREIGN KEY (employee_id)
                        REFERENCES employees(employee_id)
                        ON DELETE CASCADE
                );
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS roles (
                    role_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    role_name TEXT NOT NULL UNIQUE
                );
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS employee_roles (
                    employee_id TEXT NOT NULL,
                    role_id INTEGER NOT NULL,
                    PRIMARY KEY (employee_id, role_id),
                    FOREIGN KEY (employee_id)
                        REFERENCES employees(employee_id)
                        ON DELETE CASCADE,
                    FOREIGN KEY (role_id)
                        REFERENCES roles(role_id)
                        ON DELETE CASCADE
                );
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS employee_preferences (
                    preference_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    employee_id TEXT NOT NULL,
                    day_of_week INTEGER NOT NULL,
                    shift_type TEXT NOT NULL,
                    is_available INTEGER NOT NULL DEFAULT 1,
                    FOREIGN KEY (employee_id)
                        REFERENCES employees(employee_id)
                        ON DELETE CASCADE,
                    CHECK (day_of_week BETWEEN 1 AND 7),
                    CHECK (shift_type IN ('MORNING', 'EVENING')),
                    CHECK (is_available IN (0, 1))
                );
                """);
    }

    private static void createShiftsTables(Statement statement) throws SQLException {

        statement.execute("""
                CREATE TABLE IF NOT EXISTS shifts (
                    shift_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    shift_date TEXT NOT NULL,
                    shift_type TEXT NOT NULL,
                    start_time TEXT NOT NULL,
                    end_time TEXT NOT NULL,
                    CHECK (shift_type IN ('MORNING', 'EVENING')),
                    UNIQUE (shift_date, shift_type)
                );
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS shift_requirements (
                    shift_id INTEGER NOT NULL,
                    role_id INTEGER NOT NULL,
                    required_count INTEGER NOT NULL,
                    PRIMARY KEY (shift_id, role_id),
                    FOREIGN KEY (shift_id)
                        REFERENCES shifts(shift_id)
                        ON DELETE CASCADE,
                    FOREIGN KEY (role_id)
                        REFERENCES roles(role_id)
                        ON DELETE CASCADE,
                    CHECK (required_count > 0)
                );
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS shift_assignments (
                    shift_id INTEGER NOT NULL,
                    employee_id TEXT NOT NULL,
                    role_id INTEGER NOT NULL,
                    PRIMARY KEY (shift_id, employee_id),
                    FOREIGN KEY (shift_id)
                        REFERENCES shifts(shift_id)
                        ON DELETE CASCADE,
                    FOREIGN KEY (employee_id)
                        REFERENCES employees(employee_id)
                        ON DELETE CASCADE,
                    FOREIGN KEY (role_id)
                        REFERENCES roles(role_id)
                        ON DELETE CASCADE
                );
                """);
    }

    private static void createMockTransportTables(Statement statement) throws SQLException {

        statement.execute("""
                CREATE TABLE IF NOT EXISTS mock_trucks (
                    truck_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    license_plate TEXT NOT NULL UNIQUE,
                    model TEXT NOT NULL,
                    required_license_type TEXT NOT NULL,
                    net_weight REAL NOT NULL,
                    max_weight REAL NOT NULL,
                    CHECK (net_weight > 0),
                    CHECK (max_weight > net_weight)
                );
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS mock_sites (
                    site_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    site_name TEXT NOT NULL,
                    address TEXT NOT NULL,
                    contact_name TEXT,
                    phone TEXT
                );
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS mock_transports (
                    transport_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    transport_date TEXT NOT NULL,
                    departure_time TEXT NOT NULL,
                    source_site_id INTEGER NOT NULL,
                    truck_id INTEGER NOT NULL,
                    driver_employee_id TEXT NOT NULL,
                    shift_id INTEGER,
                    actual_weight REAL,
                    status TEXT NOT NULL DEFAULT 'APPROVED',
                    FOREIGN KEY (source_site_id)
                        REFERENCES mock_sites(site_id),
                    FOREIGN KEY (truck_id)
                        REFERENCES mock_trucks(truck_id),
                    FOREIGN KEY (driver_employee_id)
                        REFERENCES employees(employee_id),
                    FOREIGN KEY (shift_id)
                        REFERENCES shifts(shift_id),
                    CHECK (status IN ('PLANNED', 'APPROVED', 'REJECTED', 'COMPLETED')),
                    CHECK (actual_weight IS NULL OR actual_weight > 0)
                );
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS mock_transport_destinations (
                    transport_id INTEGER NOT NULL,
                    site_id INTEGER NOT NULL,
                    destination_order INTEGER NOT NULL,
                    PRIMARY KEY (transport_id, site_id),
                    FOREIGN KEY (transport_id)
                        REFERENCES mock_transports(transport_id)
                        ON DELETE CASCADE,
                    FOREIGN KEY (site_id)
                        REFERENCES mock_sites(site_id),
                    CHECK (destination_order > 0)
                );
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS mock_transport_documents (
                    document_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    transport_id INTEGER NOT NULL,
                    destination_site_id INTEGER NOT NULL,
                    document_number TEXT NOT NULL UNIQUE,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (transport_id)
                        REFERENCES mock_transports(transport_id)
                        ON DELETE CASCADE,
                    FOREIGN KEY (destination_site_id)
                        REFERENCES mock_sites(site_id)
                );
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS mock_transport_items (
                    item_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    document_id INTEGER NOT NULL,
                    item_name TEXT NOT NULL,
                    quantity INTEGER NOT NULL,
                    FOREIGN KEY (document_id)
                        REFERENCES mock_transport_documents(document_id)
                        ON DELETE CASCADE,
                    CHECK (quantity > 0)
                );
                """);
    }
}