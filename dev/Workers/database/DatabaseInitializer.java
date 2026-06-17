package dev.Workers.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import dev.Workers.domain.BranchRegistry;
import dev.Workers.domain.Objects.Branch;

/**
 * Creates the relational schema used to persist the Workers (Employees) module.
 * <p>
 * The schema is a Data-Mapper mapping of the domain objects:
 * Employee, EmployeeTerms (embedded value object), Role (polymorphic:
 * StandardRole / DriverRole), Preference, Access, Shift, Requirement,
 * assignments, extra hours, and WeekSchedule.
 * <p>
 * The Transport module is a mock external interface (see TransportModule) and
 * holds no real state, therefore it is intentionally NOT persisted here.
 */
public class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void initializeDatabase() {
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement()) {

            createBranchTable(statement);
            createEmployeeTables(statement);
            createShiftTables(statement);

            seedBranches(connection);

            System.out.println("Database tables initialized successfully.");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database.", e);
        }
    }

    private static void createBranchTable(Statement statement) throws SQLException {
        statement.execute("""
                CREATE TABLE IF NOT EXISTS branches (
                    branch_name TEXT PRIMARY KEY
                );
                """);
    }

    private static void seedBranches(Connection connection) throws SQLException {
        String sql = "INSERT OR IGNORE INTO branches (branch_name) VALUES (?);";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Branch branch : BranchRegistry.getInstance().getAllBranches()) {
                ps.setString(1, branch.getName());
                ps.executeUpdate();
            }
        }
    }

    private static void createEmployeeTables(Statement statement) throws SQLException {

        // Employee + embedded EmployeeTerms (value object, no own identity)
        statement.execute("""
                CREATE TABLE IF NOT EXISTS employees (
                    id           INTEGER PRIMARY KEY,
                    name         TEXT    NOT NULL,
                    branch_name  TEXT    NOT NULL,
                    is_manager   INTEGER NOT NULL DEFAULT 0,
                    bank_account INTEGER NOT NULL,
                    salary       REAL    NOT NULL,
                    start_date   TEXT    NOT NULL,
                    end_date     TEXT,
                    job_status   TEXT    NOT NULL,
                    salary_type  TEXT    NOT NULL,
                    rest_days    INTEGER NOT NULL,
                    day_off      TEXT    NOT NULL,
                    FOREIGN KEY (branch_name) REFERENCES branches(branch_name),
                    CHECK (is_manager IN (0, 1))
                );
                """);

        // Polymorphic role link:
        //   role_kind='STANDARD' -> role_name set, license null
        //   role_kind='DRIVER'   -> license set,   role_name null
        statement.execute("""
                CREATE TABLE IF NOT EXISTS employee_roles (
                    employee_id INTEGER NOT NULL,
                    role_kind   TEXT    NOT NULL,
                    role_name   TEXT,
                    license     TEXT,
                    PRIMARY KEY (employee_id, role_kind, role_name, license),
                    FOREIGN KEY (employee_id)
                        REFERENCES employees(id) ON DELETE CASCADE,
                    CHECK (role_kind IN ('STANDARD', 'DRIVER')),
                    CHECK ((role_kind = 'STANDARD' AND role_name IS NOT NULL AND license IS NULL)
                        OR (role_kind = 'DRIVER'   AND license   IS NOT NULL AND role_name IS NULL))
                );
                """);

        // Preference: DayOfWeek -> ShiftType (MORNING/EVENING/ANY/REST)
        statement.execute("""
                CREATE TABLE IF NOT EXISTS employee_preferences (
                    employee_id INTEGER NOT NULL,
                    day_of_week TEXT    NOT NULL,
                    shift_type  TEXT    NOT NULL,
                    PRIMARY KEY (employee_id, day_of_week),
                    FOREIGN KEY (employee_id)
                        REFERENCES employees(id) ON DELETE CASCADE,
                    CHECK (shift_type IN ('MORNING', 'EVENING', 'ANY', 'REST'))
                );
                """);

        // Access credentials (password only, per Access object)
        statement.execute("""
                CREATE TABLE IF NOT EXISTS access_credentials (
                    employee_id INTEGER PRIMARY KEY,
                    password    TEXT NOT NULL,
                    FOREIGN KEY (employee_id)
                        REFERENCES employees(id) ON DELETE CASCADE
                );
                """);
    }

    private static void createShiftTables(Statement statement) throws SQLException {

        // Shift natural key = (branch, date, type), matching domain equals/hashCode
        statement.execute("""
                CREATE TABLE IF NOT EXISTS shifts (
                    branch_name TEXT    NOT NULL,
                    shift_date  TEXT    NOT NULL,
                    shift_type  TEXT    NOT NULL,
                    has_manager INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY (branch_name, shift_date, shift_type),
                    FOREIGN KEY (branch_name) REFERENCES branches(branch_name),
                    CHECK (shift_type IN ('MORNING', 'EVENING')),
                    CHECK (has_manager IN (0, 1))
                );
                """);

        // Requirements per shift per (polymorphic) role
        statement.execute("""
                CREATE TABLE IF NOT EXISTS shift_requirements (
                    branch_name    TEXT    NOT NULL,
                    shift_date     TEXT    NOT NULL,
                    shift_type     TEXT    NOT NULL,
                    role_kind      TEXT    NOT NULL,
                    role_name      TEXT,
                    license        TEXT,
                    required_count INTEGER NOT NULL,
                    PRIMARY KEY (branch_name, shift_date, shift_type, role_kind, role_name, license),
                    FOREIGN KEY (branch_name, shift_date, shift_type)
                        REFERENCES shifts(branch_name, shift_date, shift_type) ON DELETE CASCADE,
                    CHECK (role_kind IN ('STANDARD', 'DRIVER')),
                    CHECK (required_count >= 0)
                );
                """);

        // Assignments: employee -> role within a shift
        statement.execute("""
                CREATE TABLE IF NOT EXISTS shift_assignments (
                    branch_name TEXT    NOT NULL,
                    shift_date  TEXT    NOT NULL,
                    shift_type  TEXT    NOT NULL,
                    employee_id INTEGER NOT NULL,
                    role_kind   TEXT    NOT NULL,
                    role_name   TEXT,
                    license     TEXT,
                    PRIMARY KEY (branch_name, shift_date, shift_type, employee_id),
                    FOREIGN KEY (branch_name, shift_date, shift_type)
                        REFERENCES shifts(branch_name, shift_date, shift_type) ON DELETE CASCADE,
                    FOREIGN KEY (employee_id)
                        REFERENCES employees(id) ON DELETE CASCADE,
                    CHECK (role_kind IN ('STANDARD', 'DRIVER'))
                );
                """);

        // Extra hours (morning shifts only), per AssignmentHandler.extraHours
        statement.execute("""
                CREATE TABLE IF NOT EXISTS shift_extra_hours (
                    branch_name TEXT    NOT NULL,
                    shift_date  TEXT    NOT NULL,
                    shift_type  TEXT    NOT NULL,
                    employee_id INTEGER NOT NULL,
                    hours       INTEGER NOT NULL,
                    PRIMARY KEY (branch_name, shift_date, shift_type, employee_id),
                    FOREIGN KEY (branch_name, shift_date, shift_type)
                        REFERENCES shifts(branch_name, shift_date, shift_type) ON DELETE CASCADE,
                    CHECK (hours >= 0)
                );
                """);

        // Week publication state (WeekSchedule).
        // Keyed by start-of-week (Sunday) only, matching the domain's
        // Map<LocalDate, WeekSchedule> in ShiftHandler (publication state is
        // currently shared across branches in the domain).
        statement.execute("""
                CREATE TABLE IF NOT EXISTS week_schedules (
                    start_of_week TEXT    PRIMARY KEY,
                    published     INTEGER NOT NULL DEFAULT 0,
                    CHECK (published IN (0, 1))
                );
                """);
    }
}