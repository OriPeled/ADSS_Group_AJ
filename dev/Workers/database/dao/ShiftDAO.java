package dev.Workers.database.dao;

import dev.Workers.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class ShiftDAO {

    private ShiftDAO() {
    }

    public static boolean assignEmployeeToShift(int shiftId, String employeeId, String roleName) {
        try (Connection connection = DatabaseManager.getConnection()) {

            ShiftInfo shiftInfo = findShiftInfo(connection, shiftId);
            if (shiftInfo == null) {
                System.out.println("Shift does not exist.");
                return false;
            }

            if (!EmployeeDAO.employeeExists(employeeId)) {
                System.out.println("Employee does not exist.");
                return false;
            }

            if (!EmployeeDAO.isEmployeeActive(employeeId)) {
                System.out.println("Employee is not active.");
                return false;
            }

            int roleId = findRoleId(connection, roleName);
            if (roleId == -1) {
                System.out.println("Role does not exist.");
                return false;
            }

            if (!EmployeeDAO.employeeHasRole(employeeId, roleName)) {
                System.out.println("Employee does not have the required role.");
                return false;
            }

            int dayOfWeek = LocalDate.parse(shiftInfo.shiftDate).getDayOfWeek().getValue();

            if (!EmployeeDAO.isEmployeeAvailable(employeeId, dayOfWeek, shiftInfo.shiftType)) {
                System.out.println("Employee is not available for this shift.");
                return false;
            }

            if (isEmployeeAlreadyAssigned(connection, shiftId, employeeId)) {
                System.out.println("Employee is already assigned to this shift.");
                return false;
            }

            int requiredCount = getRequiredCount(connection, shiftId, roleId);
            if (requiredCount == 0) {
                System.out.println("This role is not required in this shift.");
                return false;
            }

            int assignedCount = getAssignedCount(connection, shiftId, roleId);
            if (assignedCount >= requiredCount) {
                System.out.println("This role is already fully assigned in this shift.");
                return false;
            }

            insertShiftAssignment(connection, shiftId, employeeId, roleId);

            System.out.println("Employee assigned to shift successfully.");
            return true;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to assign employee to shift.", e);
        }
    }

    public static void printShiftAssignments(int shiftId) {
        String sql = """
                SELECT 
                    s.shift_date,
                    s.shift_type,
                    e.employee_id,
                    e.first_name,
                    e.last_name,
                    r.role_name
                FROM shift_assignments sa
                JOIN shifts s
                    ON sa.shift_id = s.shift_id
                JOIN employees e
                    ON sa.employee_id = e.employee_id
                JOIN roles r
                    ON sa.role_id = r.role_id
                WHERE sa.shift_id = ?
                ORDER BY r.role_name, e.employee_id;
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, shiftId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                boolean found = false;

                System.out.println("Assignments for shift " + shiftId + ":");
                System.out.println("--------------------------------");

                while (resultSet.next()) {
                    found = true;

                    String shiftDate = resultSet.getString("shift_date");
                    String shiftType = resultSet.getString("shift_type");
                    String employeeId = resultSet.getString("employee_id");
                    String firstName = resultSet.getString("first_name");
                    String lastName = resultSet.getString("last_name");
                    String roleName = resultSet.getString("role_name");

                    System.out.println(
                            shiftDate + " | " +
                                    shiftType + " | " +
                                    employeeId + " | " +
                                    firstName + " " + lastName + " | " +
                                    roleName
                    );
                }

                if (!found) {
                    System.out.println("No assignments found for this shift.");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to print shift assignments.", e);
        }
    }

    private static ShiftInfo findShiftInfo(Connection connection, int shiftId) throws SQLException {
        String sql = """
                SELECT shift_date, shift_type
                FROM shifts
                WHERE shift_id = ?;
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, shiftId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String shiftDate = resultSet.getString("shift_date");
                    String shiftType = resultSet.getString("shift_type");
                    return new ShiftInfo(shiftDate, shiftType);
                }

                return null;
            }
        }
    }

    private static int findRoleId(Connection connection, String roleName) throws SQLException {
        String sql = """
                SELECT role_id
                FROM roles
                WHERE role_name = ?;
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, roleName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("role_id");
                }

                return -1;
            }
        }
    }

    private static boolean isEmployeeAlreadyAssigned(
            Connection connection,
            int shiftId,
            String employeeId
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM shift_assignments
                WHERE shift_id = ?
                  AND employee_id = ?
                LIMIT 1;
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, shiftId);
            preparedStatement.setString(2, employeeId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static int getRequiredCount(
            Connection connection,
            int shiftId,
            int roleId
    ) throws SQLException {

        String sql = """
                SELECT required_count
                FROM shift_requirements
                WHERE shift_id = ?
                  AND role_id = ?;
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, shiftId);
            preparedStatement.setInt(2, roleId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("required_count");
                }

                return 0;
            }
        }
    }

    private static int getAssignedCount(
            Connection connection,
            int shiftId,
            int roleId
    ) throws SQLException {

        String sql = """
                SELECT COUNT(*) AS assigned_count
                FROM shift_assignments
                WHERE shift_id = ?
                  AND role_id = ?;
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, shiftId);
            preparedStatement.setInt(2, roleId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.getInt("assigned_count");
            }
        }
    }

    private static void insertShiftAssignment(
            Connection connection,
            int shiftId,
            String employeeId,
            int roleId
    ) throws SQLException {

        String sql = """
                INSERT INTO shift_assignments (
                    shift_id,
                    employee_id,
                    role_id
                ) VALUES (?, ?, ?);
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, shiftId);
            preparedStatement.setString(2, employeeId);
            preparedStatement.setInt(3, roleId);

            preparedStatement.executeUpdate();
        }
    }

    private static class ShiftInfo {
        private final String shiftDate;
        private final String shiftType;

        private ShiftInfo(String shiftDate, String shiftType) {
            this.shiftDate = shiftDate;
            this.shiftType = shiftType;
        }
    }
}