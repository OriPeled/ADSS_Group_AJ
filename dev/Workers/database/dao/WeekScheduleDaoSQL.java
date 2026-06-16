package dev.Workers.database.dao;

import dev.Workers.database.DatabaseManager;
import dev.Workers.domain.Objects.WeekSchedule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for {@link WeekSchedule}.
 * <p>
 * A WeekSchedule is identified by its start-of-week date (always a Sunday),
 * matching the domain's Map<LocalDate, WeekSchedule>. Maps to the
 * {@code week_schedules} table. Only the publication flag is mutable.
 */
public class WeekScheduleDaoSQL {

    private static final WeekScheduleDaoSQL instance = new WeekScheduleDaoSQL();

    private WeekScheduleDaoSQL() {
    }

    public static WeekScheduleDaoSQL getInstance() {
        return instance;
    }

    /** Loads the schedule for a given week, or null if not stored. */
    public WeekSchedule get(LocalDate startOfWeek) {
        String sql = "SELECT start_of_week, published FROM week_schedules WHERE start_of_week = ?;";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, startOfWeek.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load week schedule " + startOfWeek, e);
        }
    }

    /**
     * Loads all stored week schedules. Used to repopulate the ShiftHandler's
     * week-schedule Identity Map on startup.
     */
    public List<WeekSchedule> getAll() {
        String sql = "SELECT start_of_week, published FROM week_schedules;";
        List<WeekSchedule> result = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load week schedules.", e);
        }

        return result;
    }

    /**
     * Persists a week schedule. Uses INSERT OR REPLACE so the same call works
     * whether the week is new or already stored.
     */
    public void save(WeekSchedule weekSchedule) {
        String sql = """
                INSERT INTO week_schedules (start_of_week, published)
                VALUES (?, ?)
                ON CONFLICT(start_of_week)
                DO UPDATE SET published = excluded.published;
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, weekSchedule.getStartOfWeek().toString());
            ps.setInt(2, weekSchedule.isPublished() ? 1 : 0);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to save week schedule " + weekSchedule.getStartOfWeek(), e);
        }
    }

    /** Same semantics as save (publication flag is rewritten). */
    public void update(WeekSchedule weekSchedule) {
        save(weekSchedule);
    }

    public void delete(WeekSchedule weekSchedule) {
        String sql = "DELETE FROM week_schedules WHERE start_of_week = ?;";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, weekSchedule.getStartOfWeek().toString());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to delete week schedule " + weekSchedule.getStartOfWeek(), e);
        }
    }

    private WeekSchedule mapRow(ResultSet rs) throws SQLException {
        WeekSchedule weekSchedule = new WeekSchedule(LocalDate.parse(rs.getString("start_of_week")));
        weekSchedule.setPublished(rs.getInt("published") == 1);
        return weekSchedule;
    }
}