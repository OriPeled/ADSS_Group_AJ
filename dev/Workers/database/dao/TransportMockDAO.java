package dev.Workers.database.dao;

import dev.Workers.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TransportMockDAO {

    private TransportMockDAO() {
    }

    public static boolean createTransport(
            String transportDate,
            String departureTime,
            int sourceSiteId,
            int truckId,
            String driverEmployeeId,
            double actualWeight,
            int[] destinationSiteIds
    ) {
        try (Connection connection = DatabaseManager.getConnection()) {

            if (destinationSiteIds == null || destinationSiteIds.length == 0) {
                System.out.println("Transport must have at least one destination.");
                return false;
            }

            if (!siteExists(connection, sourceSiteId)) {
                System.out.println("Source site does not exist.");
                return false;
            }

            for (int destinationSiteId : destinationSiteIds) {
                if (!siteExists(connection, destinationSiteId)) {
                    System.out.println("Destination site does not exist: " + destinationSiteId);
                    return false;
                }
            }

            if (!truckExists(connection, truckId)) {
                System.out.println("Truck does not exist.");
                return false;
            }

            if (!driverExistsAndActive(connection, driverEmployeeId)) {
                System.out.println("Driver does not exist or is not active.");
                return false;
            }

            if (!driverHasDriverRole(connection, driverEmployeeId)) {
                System.out.println("Employee is not defined as DRIVER.");
                return false;
            }

            String requiredLicenseType = getTruckRequiredLicenseType(connection, truckId);
            String driverLicenseType = getDriverLicenseType(connection, driverEmployeeId);

            if (driverLicenseType == null || !driverLicenseType.equals(requiredLicenseType)) {
                System.out.println("Driver license does not match truck license requirement.");
                return false;
            }

            double maxWeight = getTruckMaxWeight(connection, truckId);

            if (actualWeight > maxWeight) {
                System.out.println("Actual truck weight is above the allowed maximum weight.");
                return false;
            }

            int shiftId = findShiftIdForTransport(connection, transportDate, departureTime);

            if (shiftId == -1) {
                System.out.println("No shift found for this transport date and time.");
                return false;
            }

            if (!isDriverAssignedToShift(connection, shiftId, driverEmployeeId)) {
                System.out.println("Driver is not assigned to the matching shift.");
                return false;
            }

            if (transportAlreadyExists(
                    connection,
                    transportDate,
                    departureTime,
                    sourceSiteId,
                    truckId,
                    driverEmployeeId
            )) {
                System.out.println("Transport already exists.");
                return false;
            }

            connection.setAutoCommit(false);

            try {
                int transportId = insertTransport(
                        connection,
                        transportDate,
                        departureTime,
                        sourceSiteId,
                        truckId,
                        driverEmployeeId,
                        shiftId,
                        actualWeight
                );

                for (int i = 0; i < destinationSiteIds.length; i++) {
                    int destinationOrder = i + 1;
                    int destinationSiteId = destinationSiteIds[i];

                    insertTransportDestination(
                            connection,
                            transportId,
                            destinationSiteId,
                            destinationOrder
                    );

                    insertTransportDocument(
                            connection,
                            transportId,
                            destinationSiteId,
                            destinationOrder
                    );
                }

                connection.commit();

                System.out.println("Transport created successfully. Transport ID: " + transportId);
                return true;

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create mock transport.", e);
        }
    }

    public static void printAllMockTransports() {
        String sql = """
                SELECT
                    t.transport_id,
                    t.transport_date,
                    t.departure_time,
                    source.site_name AS source_name,
                    tr.license_plate,
                    e.first_name || ' ' || e.last_name AS driver_name,
                    t.actual_weight,
                    t.status,
                    GROUP_CONCAT(destination.site_name, ', ') AS destinations
                FROM mock_transports t
                JOIN mock_sites source
                    ON t.source_site_id = source.site_id
                JOIN mock_trucks tr
                    ON t.truck_id = tr.truck_id
                JOIN employees e
                    ON t.driver_employee_id = e.employee_id
                LEFT JOIN mock_transport_destinations td
                    ON t.transport_id = td.transport_id
                LEFT JOIN mock_sites destination
                    ON td.site_id = destination.site_id
                GROUP BY
                    t.transport_id,
                    t.transport_date,
                    t.departure_time,
                    source.site_name,
                    tr.license_plate,
                    e.first_name,
                    e.last_name,
                    t.actual_weight,
                    t.status
                ORDER BY t.transport_id;
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            boolean found = false;

            System.out.println("Mock transports in database:");
            System.out.println("----------------------------");

            while (resultSet.next()) {
                found = true;

                int transportId = resultSet.getInt("transport_id");
                String transportDate = resultSet.getString("transport_date");
                String departureTime = resultSet.getString("departure_time");
                String sourceName = resultSet.getString("source_name");
                String licensePlate = resultSet.getString("license_plate");
                String driverName = resultSet.getString("driver_name");
                double actualWeight = resultSet.getDouble("actual_weight");
                String status = resultSet.getString("status");
                String destinations = resultSet.getString("destinations");

                if (destinations == null) {
                    destinations = "-";
                }

                System.out.println(
                        transportId + " | " +
                                transportDate + " " + departureTime + " | " +
                                "source: " + sourceName + " | " +
                                "truck: " + licensePlate + " | " +
                                "driver: " + driverName + " | " +
                                "weight: " + actualWeight + " | " +
                                "status: " + status + " | " +
                                "destinations: " + destinations
                );
            }

            if (!found) {
                System.out.println("No mock transports found.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to print mock transports.", e);
        }
    }

    private static boolean siteExists(Connection connection, int siteId) throws SQLException {
        String sql = """
                SELECT 1
                FROM mock_sites
                WHERE site_id = ?
                LIMIT 1;
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, siteId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static boolean truckExists(Connection connection, int truckId) throws SQLException {
        String sql = """
                SELECT 1
                FROM mock_trucks
                WHERE truck_id = ?
                LIMIT 1;
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, truckId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static boolean driverExistsAndActive(
            Connection connection,
            String driverEmployeeId
    ) throws SQLException {
        String sql = """
                SELECT 1
                FROM employees
                WHERE employee_id = ?
                  AND is_active = 1
                LIMIT 1;
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, driverEmployeeId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static boolean driverHasDriverRole(
            Connection connection,
            String driverEmployeeId
    ) throws SQLException {
        String sql = """
                SELECT 1
                FROM employee_roles er
                JOIN roles r
                    ON er.role_id = r.role_id
                WHERE er.employee_id = ?
                  AND r.role_name = 'DRIVER'
                LIMIT 1;
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, driverEmployeeId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static String getTruckRequiredLicenseType(
            Connection connection,
            int truckId
    ) throws SQLException {
        String sql = """
                SELECT required_license_type
                FROM mock_trucks
                WHERE truck_id = ?;
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, truckId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("required_license_type");
                }

                return null;
            }
        }
    }

    private static String getDriverLicenseType(
            Connection connection,
            String driverEmployeeId
    ) throws SQLException {
        String sql = """
                SELECT driver_license_type
                FROM employees
                WHERE employee_id = ?;
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, driverEmployeeId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("driver_license_type");
                }

                return null;
            }
        }
    }

    private static double getTruckMaxWeight(
            Connection connection,
            int truckId
    ) throws SQLException {
        String sql = """
                SELECT max_weight
                FROM mock_trucks
                WHERE truck_id = ?;
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, truckId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("max_weight");
                }

                return -1;
            }
        }
    }

    private static int findShiftIdForTransport(
            Connection connection,
            String transportDate,
            String departureTime
    ) throws SQLException {
        String sql = """
                SELECT shift_id
                FROM shifts
                WHERE shift_date = ?
                  AND start_time <= ?
                  AND end_time > ?
                LIMIT 1;
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, transportDate);
            preparedStatement.setString(2, departureTime);
            preparedStatement.setString(3, departureTime);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("shift_id");
                }

                return -1;
            }
        }
    }

    private static boolean isDriverAssignedToShift(
            Connection connection,
            int shiftId,
            String driverEmployeeId
    ) throws SQLException {
        String sql = """
                SELECT 1
                FROM shift_assignments sa
                JOIN roles r
                    ON sa.role_id = r.role_id
                WHERE sa.shift_id = ?
                  AND sa.employee_id = ?
                  AND r.role_name = 'DRIVER'
                LIMIT 1;
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, shiftId);
            preparedStatement.setString(2, driverEmployeeId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static boolean transportAlreadyExists(
            Connection connection,
            String transportDate,
            String departureTime,
            int sourceSiteId,
            int truckId,
            String driverEmployeeId
    ) throws SQLException {
        String sql = """
                SELECT 1
                FROM mock_transports
                WHERE transport_date = ?
                  AND departure_time = ?
                  AND source_site_id = ?
                  AND truck_id = ?
                  AND driver_employee_id = ?
                LIMIT 1;
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, transportDate);
            preparedStatement.setString(2, departureTime);
            preparedStatement.setInt(3, sourceSiteId);
            preparedStatement.setInt(4, truckId);
            preparedStatement.setString(5, driverEmployeeId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static int insertTransport(
            Connection connection,
            String transportDate,
            String departureTime,
            int sourceSiteId,
            int truckId,
            String driverEmployeeId,
            int shiftId,
            double actualWeight
    ) throws SQLException {
        String sql = """
                INSERT INTO mock_transports (
                    transport_date,
                    departure_time,
                    source_site_id,
                    truck_id,
                    driver_employee_id,
                    shift_id,
                    actual_weight,
                    status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, 'APPROVED');
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
        )) {
            preparedStatement.setString(1, transportDate);
            preparedStatement.setString(2, departureTime);
            preparedStatement.setInt(3, sourceSiteId);
            preparedStatement.setInt(4, truckId);
            preparedStatement.setString(5, driverEmployeeId);
            preparedStatement.setInt(6, shiftId);
            preparedStatement.setDouble(7, actualWeight);

            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }

                throw new SQLException("Creating transport failed, no ID obtained.");
            }
        }
    }

    private static void insertTransportDestination(
            Connection connection,
            int transportId,
            int destinationSiteId,
            int destinationOrder
    ) throws SQLException {
        String sql = """
                INSERT INTO mock_transport_destinations (
                    transport_id,
                    site_id,
                    destination_order
                ) VALUES (?, ?, ?);
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, transportId);
            preparedStatement.setInt(2, destinationSiteId);
            preparedStatement.setInt(3, destinationOrder);

            preparedStatement.executeUpdate();
        }
    }

    private static void insertTransportDocument(
            Connection connection,
            int transportId,
            int destinationSiteId,
            int destinationOrder
    ) throws SQLException {
        String documentNumber = "DOC-T" + transportId + "-D" + destinationOrder;

        String sql = """
                INSERT INTO mock_transport_documents (
                    transport_id,
                    destination_site_id,
                    document_number
                ) VALUES (?, ?, ?);
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, transportId);
            preparedStatement.setInt(2, destinationSiteId);
            preparedStatement.setString(3, documentNumber);

            preparedStatement.executeUpdate();
        }
    }
}