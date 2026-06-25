package dev.Workers.database.dao;

import dev.Workers.database.DatabaseManager;
import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.domain.Objects.Preference;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Access Object for {@link Preference}.
 * <p>
 * A Preference belongs to an employee but does not itself hold the employee id,
 * so this DAO takes the employee id explicitly rather than implementing the
 * generic {@code Dao<Preference>} signature. It maps the weekly
 * DayOfWeek -> ShiftType map to the {@code employee_preferences} table.
 */
public class PreferenceDaoSQL {
    private static final PreferenceDaoSQL instance = new PreferenceDaoSQL();

    private PreferenceDaoSQL() {
    }

    public static PreferenceDaoSQL getInstance() {
        return instance;
    }

    /** Loads the preference for a single employee, or null if none stored. */
    public Preference get(int employeeId) {
        String sql = "SELECT day_of_week, shift_type FROM employee_preferences WHERE employee_id = ?;";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, employeeId);

            Preference preference = new Preference();
            boolean found = false;

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    found = true;
                    DayOfWeek day = DayOfWeek.valueOf(rs.getString("day_of_week"));
                    ShiftType type = ShiftType.valueOf(rs.getString("shift_type"));
                    preference.setShiftType(day, type);
                }
            }

            return found ? preference : null;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load preferences for employee " + employeeId, e);
        }
    }

    /**
     * Loads every employee's preferences, keyed by employee id. Used to populate
     * the PreferenceHandler Identity Map on startup.
     */
    public Map<Integer, Preference> getAll() {
        String sql = "SELECT employee_id, day_of_week, shift_type FROM employee_preferences;";
        Map<Integer, Preference> result = new HashMap<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int employeeId = rs.getInt("employee_id");
                DayOfWeek day = DayOfWeek.valueOf(rs.getString("day_of_week"));
                ShiftType type = ShiftType.valueOf(rs.getString("shift_type"));

                result.computeIfAbsent(employeeId, k -> new Preference())
                        .setShiftType(day, type);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load all preferences.", e);
        }

        return result;
    }

    /**
     * Persists an employee's full weekly preference. Uses delete-and-reinsert so
     * the stored state always matches the in-memory state.
     */
    public void save(int employeeId, Preference preference) {
        try (Connection connection = DatabaseManager.getConnection()) {
            deleteRows(connection, employeeId);
            insertRows(connection, employeeId, preference);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save preferences for employee " + employeeId, e);
        }
    }

    /** Same as save: the weekly preference is rewritten wholesale. */
    public void update(int employeeId, Preference preference) {
        save(employeeId, preference);
    }

    /**
     * Removes all preference rows for the given employee.
     * Also removed automatically via ON DELETE CASCADE when the employee row is deleted.
     */
    public void delete(int employeeId) {
        try (Connection connection = DatabaseManager.getConnection()) {
            deleteRows(connection, employeeId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete preferences for employee " + employeeId, e);
        }
    }

    /** Deletes all preference rows for this employee. Called as the first step of a save/update. */
    private void deleteRows(Connection connection, int employeeId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM employee_preferences WHERE employee_id = ?;")) {
            ps.setInt(1, employeeId);
            ps.executeUpdate();
        }
    }

    /** Batch-inserts one row per day-of-week entry from the preference map. */
    private void insertRows(Connection connection, int employeeId, Preference preference) throws SQLException {
        String sql = """
                INSERT INTO employee_preferences (employee_id, day_of_week, shift_type)
                VALUES (?, ?, ?);
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Map.Entry<DayOfWeek, ShiftType> entry : preference.getWeekPreferences().entrySet()) {
                ps.setInt(1, employeeId);
                ps.setString(2, entry.getKey().name());
                ps.setString(3, entry.getValue().name());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}