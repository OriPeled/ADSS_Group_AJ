package dev.Workers.database.dao;

import dev.Workers.database.DatabaseManager;
import dev.Workers.domain.Objects.Access;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Access Object for {@link Access} (login credentials).
 * <p>
 * Like preferences, an Access object belongs to an employee but does not hold
 * the employee id, so this DAO takes the id explicitly. Maps to the
 * {@code access_credentials} table.
 */
public class AccessDaoSQL {
    private static final AccessDaoSQL instance = new AccessDaoSQL();

    private AccessDaoSQL() {
    }

    public static AccessDaoSQL getInstance() {
        return instance;
    }

    /** Loads the credentials for a single employee, or null if not registered. */
    public Access get(int employeeId) {
        String sql = "SELECT password FROM access_credentials WHERE employee_id = ?;";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, employeeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Access(rs.getString("password"));
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load credentials for employee " + employeeId, e);
        }
    }

    /**
     * Loads all registered credentials keyed by employee id. Used to populate
     * the AccessHandler Identity Map on startup.
     */
    public Map<Integer, Access> getAll() {
        String sql = "SELECT employee_id, password FROM access_credentials;";
        Map<Integer, Access> result = new HashMap<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.put(rs.getInt("employee_id"), new Access(rs.getString("password")));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load all credentials.", e);
        }

        return result;
    }

    public void save(int employeeId, Access access) {
        String sql = "INSERT INTO access_credentials (employee_id, password) VALUES (?, ?);";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, employeeId);
            ps.setString(2, access.getPassword());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save credentials for employee " + employeeId, e);
        }
    }

    public void update(int employeeId, Access access) {
        String sql = "UPDATE access_credentials SET password = ? WHERE employee_id = ?;";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, access.getPassword());
            ps.setInt(2, employeeId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update credentials for employee " + employeeId, e);
        }
    }

    public void delete(int employeeId) {
        String sql = "DELETE FROM access_credentials WHERE employee_id = ?;";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, employeeId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete credentials for employee " + employeeId, e);
        }
    }
}