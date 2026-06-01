package dev.Workers.database.dao;

import dev.Workers.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeeDAO {

    private EmployeeDAO() {
    }

    public static void printAllEmployees() {
        String sql = """
                SELECT 
                    e.employee_id,
                    e.first_name,
                    e.last_name,
                    e.is_active,
                    COALESCE(e.driver_license_type, '-') AS driver_license_type,
                    GROUP_CONCAT(r.role_name, ', ') AS roles
                FROM employees e
                LEFT JOIN employee_roles er 
                    ON e.employee_id = er.employee_id
                LEFT JOIN roles r 
                    ON er.role_id = r.role_id
                GROUP BY 
                    e.employee_id,
                    e.first_name,
                    e.last_name,
                    e.is_active,
                    e.driver_license_type
                ORDER BY e.employee_id;
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            boolean foundEmployees = false;

            System.out.println("Employees in database:");
            System.out.println("----------------------");

            while (resultSet.next()) {
                foundEmployees = true;

                String employeeId = resultSet.getString("employee_id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                boolean active = resultSet.getInt("is_active") == 1;
                String licenseType = resultSet.getString("driver_license_type");
                String roles = resultSet.getString("roles");

                if (roles == null) {
                    roles = "-";
                }

                System.out.println(
                        employeeId + " | " +
                                firstName + " " + lastName + " | " +
                                "active: " + active + " | " +
                                "license: " + licenseType + " | " +
                                "roles: " + roles
                );
            }

            if (!foundEmployees) {
                System.out.println("No employees found. Load demo data first.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to read employees from database.", e);
        }
    }

    public static boolean employeeExists(String employeeId) {
        String sql = """
                SELECT 1
                FROM employees
                WHERE employee_id = ?
                LIMIT 1;
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, employeeId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check if employee exists.", e);
        }
    }

    public static boolean isEmployeeActive(String employeeId) {
        String sql = """
                SELECT is_active
                FROM employees
                WHERE employee_id = ?;
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, employeeId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() && resultSet.getInt("is_active") == 1;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check if employee is active.", e);
        }
    }

    public static boolean employeeHasRole(String employeeId, String roleName) {
        String sql = """
                SELECT 1
                FROM employee_roles er
                JOIN roles r 
                    ON er.role_id = r.role_id
                WHERE er.employee_id = ?
                  AND r.role_name = ?
                LIMIT 1;
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, employeeId);
            preparedStatement.setString(2, roleName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check employee role.", e);
        }
    }

    public static boolean isEmployeeAvailable(String employeeId, int dayOfWeek, String shiftType) {
        String sql = """
                SELECT 1
                FROM employee_constraints
                WHERE employee_id = ?
                  AND day_of_week = ?
                  AND shift_type = ?
                  AND is_available = 1
                LIMIT 1;
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, employeeId);
            preparedStatement.setInt(2, dayOfWeek);
            preparedStatement.setString(3, shiftType);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check employee availability.", e);
        }
    }
}