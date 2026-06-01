package dev.Workers.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SeedData {

    private SeedData() {
    }

    public static void loadDemoData() {
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement()) {

            insertRoles(statement);
            insertEmployees(statement);
            insertEmployeeRoles(statement);
            insertEmployeeConstraints(statement);
            insertShifts(statement);
            insertShiftRequirements(statement);
            insertShiftAssignments(statement);

            insertMockTrucks(statement);
            insertMockSites(statement);
            insertMockTransports(statement);
            insertMockTransportDestinations(statement);
            insertMockTransportDocuments(statement);
            insertMockTransportItems(statement);

            System.out.println("Demo data loaded successfully.");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load demo data.", e);
        }
    }

    private static void insertRoles(Statement statement) throws SQLException {
        statement.execute("""
                INSERT OR IGNORE INTO roles (role_id, role_name) VALUES
                (1, 'SHIFT_MANAGER'),
                (2, 'CASHIER'),
                (3, 'WAREHOUSE_WORKER'),
                (4, 'DRIVER');
                """);
    }

    private static void insertEmployees(Statement statement) throws SQLException {
        statement.execute("""
                INSERT OR IGNORE INTO employees (
                    employee_id,
                    first_name,
                    last_name,
                    bank_account,
                    salary,
                    employment_terms,
                    start_date,
                    is_active,
                    driver_license_type
                ) VALUES
                ('111111111', 'Avi', 'Cohen', '123-456', 45.0, 'Full time', '2025-01-01', 1, NULL),
                ('222222222', 'Dana', 'Levi', '234-567', 42.0, 'Part time', '2025-02-01', 1, NULL),
                ('333333333', 'Moshe', 'Israeli', '345-678', 48.0, 'Full time', '2025-03-01', 1, NULL),
                ('444444444', 'Yossi', 'Driver', '456-789', 50.0, 'Full time', '2025-04-01', 1, 'C1'),
                ('555555555', 'Noam', 'Driver', '567-890', 55.0, 'Full time', '2025-05-01', 1, 'C');
                """);
    }

    private static void insertEmployeeRoles(Statement statement) throws SQLException {
        statement.execute("""
                INSERT OR IGNORE INTO employee_roles (employee_id, role_id) VALUES
                ('111111111', 1),
                ('222222222', 2),
                ('333333333', 3),
                ('444444444', 4),
                ('555555555', 4);
                """);
    }

    private static void insertEmployeeConstraints(Statement statement) throws SQLException {
        statement.execute("""
                INSERT OR IGNORE INTO employee_constraints (
                    employee_id,
                    day_of_week,
                    shift_type,
                    is_available
                ) VALUES
                ('111111111', 1, 'MORNING', 1),
                ('222222222', 1, 'MORNING', 1),
                ('333333333', 1, 'MORNING', 1),
                ('444444444', 1, 'MORNING', 1),
                ('555555555', 1, 'EVENING', 1);
                """);
    }

    private static void insertShifts(Statement statement) throws SQLException {
        statement.execute("""
                INSERT OR IGNORE INTO shifts (
                    shift_id,
                    shift_date,
                    shift_type,
                    start_time,
                    end_time
                ) VALUES
                (1, '2026-06-01', 'MORNING', '06:00', '14:00'),
                (2, '2026-06-01', 'EVENING', '14:00', '22:00');
                """);
    }

    private static void insertShiftRequirements(Statement statement) throws SQLException {
        statement.execute("""
            INSERT OR IGNORE INTO shift_requirements (
                shift_id,
                role_id,
                required_count
            ) VALUES
            (1, 1, 1),
            (1, 2, 1),
            (1, 3, 1),
            (1, 4, 1),
            (2, 1, 1),
            (2, 3, 1),
            (2, 4, 1);
            """);
    }

    private static void insertShiftAssignments(Statement statement) throws SQLException {
        statement.execute("""
                INSERT OR IGNORE INTO shift_assignments (
                    shift_id,
                    employee_id,
                    role_id
                ) VALUES
                (1, '111111111', 1),
                (1, '222222222', 2),
                (1, '333333333', 3),
                (1, '444444444', 4);
                """);
    }

    private static void insertMockTrucks(Statement statement) throws SQLException {
        statement.execute("""
                INSERT OR IGNORE INTO mock_trucks (
                    truck_id,
                    license_plate,
                    model,
                    required_license_type,
                    net_weight,
                    max_weight
                ) VALUES
                (1, '123-45-678', 'Isuzu N-Series', 'C1', 3500, 7500),
                (2, '987-65-432', 'Mercedes Actros', 'C', 8000, 18000);
                """);
    }

    private static void insertMockSites(Statement statement) throws SQLException {
        statement.execute("""
                INSERT OR IGNORE INTO mock_sites (
                    site_id,
                    site_name,
                    address,
                    contact_name,
                    phone
                ) VALUES
                (1, 'SuperLi Warehouse', 'HaTaasiya 10, Holon', 'Rami', '03-1111111'),
                (2, 'SuperLi Branch Jerusalem', 'Jaffa 20, Jerusalem', 'Shira', '02-2222222'),
                (3, 'Tnuva Supplier', 'HaMilk 5, Rehovot', 'Eli', '08-3333333');
                """);
    }

    private static void insertMockTransports(Statement statement) throws SQLException {
        statement.execute("""
                INSERT OR IGNORE INTO mock_transports (
                    transport_id,
                    transport_date,
                    departure_time,
                    source_site_id,
                    truck_id,
                    driver_employee_id,
                    shift_id,
                    actual_weight,
                    status
                ) VALUES
                (1, '2026-06-01', '08:00', 1, 1, '444444444', 1, 6200, 'APPROVED');
                """);
    }

    private static void insertMockTransportDestinations(Statement statement) throws SQLException {
        statement.execute("""
                INSERT OR IGNORE INTO mock_transport_destinations (
                    transport_id,
                    site_id,
                    destination_order
                ) VALUES
                (1, 2, 1),
                (1, 3, 2);
                """);
    }

    private static void insertMockTransportDocuments(Statement statement) throws SQLException {
        statement.execute("""
                INSERT OR IGNORE INTO mock_transport_documents (
                    document_id,
                    transport_id,
                    destination_site_id,
                    document_number
                ) VALUES
                (1, 1, 2, 'DOC-2026-0001'),
                (2, 1, 3, 'DOC-2026-0002');
                """);
    }

    private static void insertMockTransportItems(Statement statement) throws SQLException {
        statement.execute("""
                INSERT OR IGNORE INTO mock_transport_items (
                    item_id,
                    document_id,
                    item_name,
                    quantity
                ) VALUES
                (1, 1, 'Milk 3%', 120),
                (2, 1, 'Cheese 250g', 80),
                (3, 2, 'Empty Crates', 30);
                """);
    }
}