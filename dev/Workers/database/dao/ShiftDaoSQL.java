package dev.Workers.database.dao;

import dev.Workers.database.DatabaseManager;
import dev.Workers.domain.AssignmentHandler;
import dev.Workers.domain.BranchRegistry;
import dev.Workers.domain.RequirementHandler;
import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.domain.Objects.Branch;
import dev.Workers.domain.Objects.Requirement;
import dev.Workers.domain.Objects.Role;
import dev.Workers.domain.Objects.Shift;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Data Access Object for {@link Shift} and everything that hangs off a shift:
 * its requirements, its employee assignments, and its (morning-only) extra
 * hours.
 * <p>
 * A shift is identified by its natural key (branch, date, type), matching the
 * domain's equals/hashCode. Because the shift's data is spread across three
 * domain Handlers (ShiftHandler, RequirementHandler, AssignmentHandler), this
 * DAO writes to four tables on save and rebuilds the Handlers on load.
 * <p>
 * Approval process state in AssignmentHandler (pending requests / request
 * answers) is persisted separately by RequestDaoSQL, so it survives restarts.
 */
public class ShiftDaoSQL {
    private static final ShiftDaoSQL instance = new ShiftDaoSQL();

    private final BranchRegistry branchRegistry = BranchRegistry.getInstance();

    private ShiftDaoSQL() {
    }

    public static ShiftDaoSQL getInstance() {
        return instance;
    }

    // ------------------------------------------------------------------
    // save / update one shift (with its requirements, assignments, hours)
    // ------------------------------------------------------------------

    /**
     * Persists a shift and all of its associated data. Uses INSERT OR REPLACE on
     * the shift row and delete-and-reinsert for the dependent collections, so a
     * single call writes the complete current state of the shift.
     */
    public void save(Shift shift,
                     List<Requirement> requirements,
                     Map<Role, Set<Integer>> assignmentsByRole,
                     Map<Integer, Integer> extraHours) {

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                upsertShift(connection, shift);
                replaceRequirements(connection, shift, requirements);
                replaceAssignments(connection, shift, assignmentsByRole);
                replaceExtraHours(connection, shift, extraHours);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save shift " + describe(shift), e);
        }
    }

    /** Same semantics as save (full rewrite of the shift's state). */
    public void update(Shift shift,
                       List<Requirement> requirements,
                       Map<Role, Set<Integer>> assignmentsByRole,
                       Map<Integer, Integer> extraHours) {
        save(shift, requirements, assignmentsByRole, extraHours);
    }

    /**
     * Deletes the shift row. All dependent rows (requirements, assignments, extra hours)
     * are removed automatically via ON DELETE CASCADE.
     */
    public void delete(Shift shift) {
        // dependent rows are removed via ON DELETE CASCADE
        String sql = "DELETE FROM shifts WHERE branch_name = ? AND shift_date = ? AND shift_type = ?;";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            bindShiftKey(ps, 1, shift);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete shift " + describe(shift), e);
        }
    }

    // ------------------------------------------------------------------
    // load: rebuild the Handlers from the DB
    // ------------------------------------------------------------------

    /**
     * Loads every stored shift and repopulates the given Handlers (the Identity
     * Maps). Returns the list of loaded shifts so the caller can also repopulate
     * the ShiftHandler's shift set. Called once on startup.
     */
    public List<Shift> loadAll(RequirementHandler requirementHandler,
                               AssignmentHandler assignmentHandler) {

        try (Connection connection = DatabaseManager.getConnection()) {

            List<Shift> shifts = loadShifts(connection);

            for (Shift shift : shifts) {
                // initialize empty requirement/assignment structures for the shift
                requirementHandler.defaultInit(shift);
                assignmentHandler.init(shift);

                loadRequirements(connection, shift, requirementHandler);
                loadAssignments(connection, shift, assignmentHandler);
                loadExtraHours(connection, shift, assignmentHandler);
            }

            return shifts;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load shifts.", e);
        }
    }

    /** Returns all stored shifts (used to repopulate ShiftHandler's shift set). */
    public List<Shift> getAllShifts() {
        try (Connection connection = DatabaseManager.getConnection()) {
            return loadShifts(connection);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load shifts.", e);
        }
    }

    // ------------------------------------------------------------------
    // shift row
    // ------------------------------------------------------------------

    /**
     * Inserts the shift row or updates has_manager in place if the row already exists.
     * has_manager is the only mutable column on the shift row itself.
     */
    private void upsertShift(Connection connection, Shift shift) throws SQLException {
        String sql = """
                INSERT INTO shifts (branch_name, shift_date, shift_type, has_manager)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(branch_name, shift_date, shift_type)
                DO UPDATE SET has_manager = excluded.has_manager;
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, shift.getBranch().getName());
            ps.setString(2, shift.getDate().toString());
            ps.setString(3, shift.getType().name());
            ps.setInt(4, shift.hasManager() ? 1 : 0);
            ps.executeUpdate();
        }
    }

    /** Reads all shift rows and constructs Shift objects, resolving each branch via BranchRegistry. */
    private List<Shift> loadShifts(Connection connection) throws SQLException {
        String sql = "SELECT branch_name, shift_date, shift_type, has_manager FROM shifts;";
        List<Shift> shifts = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Branch branch = branchRegistry.getBranchByName(rs.getString("branch_name"));
                LocalDate date = LocalDate.parse(rs.getString("shift_date"));
                ShiftType type = ShiftType.valueOf(rs.getString("shift_type"));

                Shift shift = new Shift(branch, date, type);
                shift.setManaged(rs.getInt("has_manager") == 1);
                shifts.add(shift);
            }
        }

        return shifts;
    }

    // ------------------------------------------------------------------
    // requirements
    // ------------------------------------------------------------------

    /**
     * Deletes all requirement rows for this shift, then inserts the current list.
     * This delete-and-reinsert keeps the DB in sync with the in-memory state.
     */
    private void replaceRequirements(Connection connection, Shift shift,
                                     List<Requirement> requirements) throws SQLException {
        deleteByShift(connection, "shift_requirements", shift);
        if (requirements == null) return;

        String sql = """
                INSERT INTO shift_requirements
                    (branch_name, shift_date, shift_type, role_kind, role_name, license, required_count)
                VALUES (?, ?, ?, ?, ?, ?, ?);
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Requirement requirement : requirements) {
                Role role = requirement.getRole();
                bindShiftKey(ps, 1, shift);
                ps.setString(4, RoleMapper.kindOf(role));
                setNullableString(ps, 5, RoleMapper.roleNameOf(role));
                setNullableString(ps, 6, RoleMapper.licenseOf(role));
                ps.setInt(7, requirement.getAmount());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** Reads shift_requirements for the given shift and calls requirementHandler.manualSet for each row. */
    private void loadRequirements(Connection connection, Shift shift,
                                  RequirementHandler requirementHandler) throws SQLException {
        String sql = """
                SELECT role_kind, role_name, license, required_count
                FROM shift_requirements
                WHERE branch_name = ? AND shift_date = ? AND shift_type = ?;
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bindShiftKey(ps, 1, shift);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Role role = RoleMapper.resolve(
                            rs.getString("role_kind"),
                            rs.getString("role_name"),
                            rs.getString("license"));
                    requirementHandler.manualSet(shift, role, rs.getInt("required_count"));
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // assignments
    // ------------------------------------------------------------------

    /**
     * Deletes all assignment rows for this shift, then inserts the current role-to-employees map.
     * One row is written per (role, employee) pair.
     */
    private void replaceAssignments(Connection connection, Shift shift,
                                    Map<Role, Set<Integer>> assignmentsByRole) throws SQLException {
        deleteByShift(connection, "shift_assignments", shift);
        if (assignmentsByRole == null) return;

        String sql = """
                INSERT INTO shift_assignments
                    (branch_name, shift_date, shift_type, employee_id, role_kind, role_name, license)
                VALUES (?, ?, ?, ?, ?, ?, ?);
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Map.Entry<Role, Set<Integer>> entry : assignmentsByRole.entrySet()) {
                Role role = entry.getKey();
                for (int employeeId : entry.getValue()) {
                    bindShiftKey(ps, 1, shift);
                    ps.setInt(4, employeeId);
                    ps.setString(5, RoleMapper.kindOf(role));
                    setNullableString(ps, 6, RoleMapper.roleNameOf(role));
                    setNullableString(ps, 7, RoleMapper.licenseOf(role));
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        }
    }

    /** Reads shift_assignments for the given shift and calls assignmentHandler.add for each row. */
    private void loadAssignments(Connection connection, Shift shift,
                                 AssignmentHandler assignmentHandler) throws SQLException {
        String sql = """
                SELECT employee_id, role_kind, role_name, license
                FROM shift_assignments
                WHERE branch_name = ? AND shift_date = ? AND shift_type = ?;
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bindShiftKey(ps, 1, shift);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Role role = RoleMapper.resolve(
                            rs.getString("role_kind"),
                            rs.getString("role_name"),
                            rs.getString("license"));
                    assignmentHandler.add(shift, role, rs.getInt("employee_id"));
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // extra hours
    // ------------------------------------------------------------------

    /** Deletes all extra-hour rows for this shift, then inserts the current employee-to-hours map. */
    private void replaceExtraHours(Connection connection, Shift shift,
                                   Map<Integer, Integer> extraHours) throws SQLException {
        deleteByShift(connection, "shift_extra_hours", shift);
        if (extraHours == null) return;

        String sql = """
                INSERT INTO shift_extra_hours
                    (branch_name, shift_date, shift_type, employee_id, hours)
                VALUES (?, ?, ?, ?, ?);
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Map.Entry<Integer, Integer> entry : extraHours.entrySet()) {
                bindShiftKey(ps, 1, shift);
                ps.setInt(4, entry.getKey());
                ps.setInt(5, entry.getValue());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Reads extra-hour rows and calls assignmentHandler.updateExtraHours for each.
     * Skipped for evening shifts - extra hours only apply to morning shifts.
     */
    private void loadExtraHours(Connection connection, Shift shift,
                                AssignmentHandler assignmentHandler) throws SQLException {
        if (shift.getType() != ShiftType.MORNING) {
            return; // extra hours exist for morning shifts only
        }

        String sql = """
                SELECT employee_id, hours
                FROM shift_extra_hours
                WHERE branch_name = ? AND shift_date = ? AND shift_type = ?;
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bindShiftKey(ps, 1, shift);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    assignmentHandler.updateExtraHours(
                            shift, rs.getInt("employee_id"), rs.getInt("hours"));
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    /** Deletes all rows from the given table that match the shift's natural key (branch, date, type). */
    private void deleteByShift(Connection connection, String table, Shift shift) throws SQLException {
        String sql = "DELETE FROM " + table
                + " WHERE branch_name = ? AND shift_date = ? AND shift_type = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bindShiftKey(ps, 1, shift);
            ps.executeUpdate();
        }
    }

    /** Binds the (branch, date, type) natural key starting at the given index. */
    private void bindShiftKey(PreparedStatement ps, int startIndex, Shift shift) throws SQLException {
        ps.setString(startIndex, shift.getBranch().getName());
        ps.setString(startIndex + 1, shift.getDate().toString());
        ps.setString(startIndex + 2, shift.getType().name());
    }

    /** Sets a VARCHAR parameter to SQL NULL when value is null, or to the string value otherwise. */
    private static void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }

    /** Returns a short readable description of the shift, used in error messages. */
    private String describe(Shift shift) {
        return shift.getBranch().getName() + " " + shift.getDate() + " " + shift.getType();
    }
}