package dev.Workers.database.dao;

import dev.Workers.database.DatabaseManager;
import dev.Workers.domain.Actions.RequestAction;
import dev.Workers.domain.BranchRegistry;
import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.domain.Objects.Branch;
import dev.Workers.domain.Objects.Role;
import dev.Workers.domain.Objects.Shift;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Data Access Object for the transient-but-now-persisted approval state held by
 * {@link dev.Workers.domain.AssignmentHandler}: the per-employee queue of
 * pending {@link RequestAction}s and the per-branch queue of HR answer messages.
 * <p>
 * Both are queues, so each row carries a {@code seq} column to preserve FIFO
 * order. Following the same strategy as {@link ShiftDaoSQL}, every save is a
 * full delete-and-reinsert of the relevant queue, so a single call writes the
 * complete current state.
 * <p>
 * A pending request references a {@link Shift}; on load it is resolved back to
 * the canonical in-memory Shift instance via a resolver supplied by the caller,
 * so the restored RequestAction keys on the same Shift the AssignmentHandler
 * uses.
 */
public class RequestDaoSQL {
    private static final RequestDaoSQL instance = new RequestDaoSQL();

    private final BranchRegistry branchRegistry = BranchRegistry.getInstance();

    private RequestDaoSQL() {
    }

    public static RequestDaoSQL getInstance() {
        return instance;
    }

    /** Resolves a stored shift natural key back to the canonical Shift instance. */
    public interface ShiftResolver {
        Shift resolve(Branch branch, LocalDate date, ShiftType type);
    }

    // ==================================================================
    // pending requests
    // ==================================================================

    /**
     * Rewrites the entire pending-requests table from the given map
     * (empId -> ordered queue of actions). Called whenever the in-memory
     * pending requests change.
     */
    public void saveAllPendingRequests(Map<Integer, Queue<RequestAction>> pendingRequests) {
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                deleteAll(connection, "pending_requests");
                insertPendingRequests(connection, pendingRequests);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save pending requests.", e);
        }
    }

    /**
     * Batch-inserts every pending request. ASSIGN actions store role columns and leave
     * current_id/new_id null. REPLACE actions store current_id/new_id and leave role columns null.
     * The seq counter preserves the FIFO order of each employee's queue.
     */
    private void insertPendingRequests(Connection connection,
                                       Map<Integer, Queue<RequestAction>> pendingRequests) throws SQLException {
        if (pendingRequests == null) return;

        String sql = """
                INSERT INTO pending_requests
                    (employee_id, seq, branch_name, shift_date, shift_type,
                     action_kind, role_kind, role_name, license, current_id, new_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Map.Entry<Integer, Queue<RequestAction>> entry : pendingRequests.entrySet()) {
                int employeeId = entry.getKey();
                Queue<RequestAction> queue = entry.getValue();
                if (queue == null) continue;

                int seq = 0;
                for (RequestAction action : queue) {
                    Shift shift = action.shift();
                    ps.setInt(1, employeeId);
                    ps.setInt(2, seq++);
                    ps.setString(3, shift.getBranch().getName());
                    ps.setString(4, shift.getDate().toString());
                    ps.setString(5, shift.getType().name());

                    if (action instanceof RequestAction.AssignAction assign) {
                        Role role = assign.role();
                        ps.setString(6, "ASSIGN");
                        ps.setString(7, RoleMapper.kindOf(role));
                        setNullableString(ps, 8, RoleMapper.roleNameOf(role));
                        setNullableString(ps, 9, RoleMapper.licenseOf(role));
                        ps.setNull(10, java.sql.Types.INTEGER);
                        ps.setNull(11, java.sql.Types.INTEGER);
                    } else if (action instanceof RequestAction.ReplaceAction replace) {
                        ps.setString(6, "REPLACE");
                        ps.setNull(7, java.sql.Types.VARCHAR);
                        ps.setNull(8, java.sql.Types.VARCHAR);
                        ps.setNull(9, java.sql.Types.VARCHAR);
                        ps.setInt(10, replace.curId());
                        ps.setInt(11, replace.newId());
                    } else {
                        throw new IllegalStateException(
                                "Unknown RequestAction type: " + action.getClass().getName());
                    }
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        }
    }

    /**
     * Loads all pending requests, ordered by (employee_id, seq), rebuilding each
     * employee's FIFO queue. Shifts are resolved to canonical instances via the
     * supplied resolver.
     */
    public Map<Integer, Queue<RequestAction>> loadAllPendingRequests(ShiftResolver resolver) {
        Map<Integer, Queue<RequestAction>> result = new LinkedHashMap<>();

        String sql = """
                SELECT employee_id, seq, branch_name, shift_date, shift_type,
                       action_kind, role_kind, role_name, license, current_id, new_id
                FROM pending_requests
                ORDER BY employee_id, seq;
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int employeeId = rs.getInt("employee_id");

                Branch branch = branchRegistry.getBranchByName(rs.getString("branch_name"));
                LocalDate date = LocalDate.parse(rs.getString("shift_date"));
                ShiftType type = ShiftType.valueOf(rs.getString("shift_type"));
                Shift shift = resolver.resolve(branch, date, type);

                RequestAction action;
                String kind = rs.getString("action_kind");
                if ("ASSIGN".equals(kind)) {
                    Role role = RoleMapper.resolve(
                            rs.getString("role_kind"),
                            rs.getString("role_name"),
                            rs.getString("license"));
                    action = new RequestAction.AssignAction(shift, role, employeeId);
                } else {
                    action = new RequestAction.ReplaceAction(
                            shift, rs.getInt("current_id"), rs.getInt("new_id"));
                }

                result.computeIfAbsent(employeeId, k -> new LinkedList<>()).add(action);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load pending requests.", e);
        }

        return result;
    }

    // ==================================================================
    // request answers
    // ==================================================================

    /**
     * Rewrites the entire request-answers table from the given map
     * (branch -> ordered queue of messages). Called whenever the in-memory
     * request answers change.
     */
    public void saveAllRequestAnswers(Map<Branch, Queue<String>> requestAnswers) {
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                deleteAll(connection, "request_answers");
                insertRequestAnswers(connection, requestAnswers);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save request answers.", e);
        }
    }

    /**
     * Batch-inserts all answer messages, keyed by branch name and position in the queue (seq).
     * The seq counter preserves the FIFO order of each branch's answer queue.
     */
    private void insertRequestAnswers(Connection connection,
                                      Map<Branch, Queue<String>> requestAnswers) throws SQLException {
        if (requestAnswers == null) return;

        String sql = """
                INSERT INTO request_answers (branch_name, seq, message)
                VALUES (?, ?, ?);
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Map.Entry<Branch, Queue<String>> entry : requestAnswers.entrySet()) {
                String branchName = entry.getKey().getName();
                Queue<String> queue = entry.getValue();
                if (queue == null) continue;

                int seq = 0;
                for (String message : queue) {
                    ps.setString(1, branchName);
                    ps.setInt(2, seq++);
                    ps.setString(3, message);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        }
    }

    /**
     * Loads all request answers, ordered by (branch_name, seq), rebuilding each
     * branch's FIFO queue.
     */
    public Map<Branch, Queue<String>> loadAllRequestAnswers() {
        Map<Branch, Queue<String>> result = new LinkedHashMap<>();

        String sql = """
                SELECT branch_name, seq, message
                FROM request_answers
                ORDER BY branch_name, seq;
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Branch branch = branchRegistry.getBranchByName(rs.getString("branch_name"));
                result.computeIfAbsent(branch, k -> new LinkedList<>())
                        .add(rs.getString("message"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load request answers.", e);
        }

        return result;
    }

    // ==================================================================
    // helpers
    // ==================================================================

    /** Deletes all rows from the given table. Used before a full re-insert of a queue. */
    private void deleteAll(Connection connection, String table) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM " + table + ";")) {
            ps.executeUpdate();
        }
    }

    /** Sets a VARCHAR parameter to SQL NULL when value is null, or to the string value otherwise. */
    private static void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }
}