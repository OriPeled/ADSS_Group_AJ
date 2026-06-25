package dev.Workers.database.dao;

import dev.Workers.database.DatabaseManager;
import dev.Workers.domain.BranchRegistry;
import dev.Workers.domain.Enums.JobStatus;
import dev.Workers.domain.Enums.SalaryType;
import dev.Workers.domain.Objects.Branch;
import dev.Workers.domain.Objects.Employee;
import dev.Workers.domain.Objects.EmployeeTerms;
import dev.Workers.domain.Objects.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for {@link Employee}.
 * <p>
 * Maps an Employee (with its embedded {@link EmployeeTerms} value object and its
 * set of polymorphic {@link Role}s) onto the {@code employees} and
 * {@code employee_roles} tables. Roles are reconstructed against the
 * RoleRegistry, and the branch against the BranchRegistry, so that restored
 * objects share identity with the in-memory registries.
 */
public class EmployeeDaoSQL implements Dao<Employee> {
    private static final EmployeeDaoSQL instance = new EmployeeDaoSQL();

    private final BranchRegistry branchRegistry = BranchRegistry.getInstance();

    private EmployeeDaoSQL() {
    }

    public static EmployeeDaoSQL getInstance() {
        return instance;
    }

    /** Returns the employee with the given id, with roles loaded. Empty if not found. */
    @Override
    public Optional<Employee> get(long id) {
        String sql = "SELECT * FROM employees WHERE id = ?;";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, (int) id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Employee employee = mapRow(rs);
                loadRoles(connection, employee);
                return Optional.of(employee);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load employee " + id, e);
        }
    }

    /** Returns all employees ordered by id, each with their roles loaded. Used on startup to populate EmployeeHandler. */
    @Override
    public List<Employee> getAll() {
        String sql = "SELECT * FROM employees ORDER BY id;";
        List<Employee> employees = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Employee employee = mapRow(rs);
                loadRoles(connection, employee);
                employees.add(employee);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load employees.", e);
        }

        return employees;
    }

    /**
     * Inserts a new employee row and all of their roles.
     * EmployeeHandler is the in-memory source of truth; this call mirrors it to the DB.
     */
    @Override
    public void save(Employee employee) {
        String sql = """
                INSERT INTO employees (
                    id, name, branch_name, is_manager, bank_account, salary,
                    start_date, end_date,
                    job_status, salary_type, rest_days, day_off
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            bindEmployee(ps, employee);
            ps.executeUpdate();

            saveRoles(connection, employee);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save employee " + employee.getId(), e);
        }
    }

    /**
     * Overwrites all mutable fields on the employee row and replaces their role set.
     * Called by EmployeeHandler after any employee data or role change.
     */
    @Override
    public void update(Employee employee) {
        String sql = """
                UPDATE employees SET
                    name = ?, branch_name = ?, is_manager = ?, bank_account = ?,
                    salary = ?, start_date = ?, end_date = ?,
                    job_status = ?, salary_type = ?, rest_days = ?, day_off = ?
                WHERE id = ?;
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, employee.getName());
            ps.setString(2, employee.getBranch().getName());
            ps.setInt(3, employee.isManager() ? 1 : 0);
            ps.setInt(4, employee.getBankAccount());
            ps.setDouble(5, employee.getSalary());
            ps.setString(6, employee.getStartLocalDate().toString());
            ps.setString(7, employee.getEndLocalDate() == null
                    ? null : employee.getEndLocalDate().toString());

            EmployeeTerms terms = employee.getTerms();
            ps.setString(8, terms.getJobStatus().name());
            ps.setString(9, terms.getSalaryType().name());
            ps.setInt(10, terms.getRestDays());
            ps.setString(11, terms.getDayOff().name());

            ps.setInt(12, employee.getId());

            ps.executeUpdate();

            replaceRoles(connection, employee);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update employee " + employee.getId(), e);
        }
    }

    @Override
    public void delete(Employee employee) {
        // employee_roles rows are removed automatically via ON DELETE CASCADE
        String sql = "DELETE FROM employees WHERE id = ?;";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, employee.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete employee " + employee.getId(), e);
        }
    }

    // ------------------------------------------------------------------
    // mapping helpers
    // ------------------------------------------------------------------

    /** Sets all 12 positional parameters for an INSERT into the employees table. */
    private void bindEmployee(PreparedStatement ps, Employee employee) throws SQLException {
        ps.setInt(1, employee.getId());
        ps.setString(2, employee.getName());
        ps.setString(3, employee.getBranch().getName());
        ps.setInt(4, employee.isManager() ? 1 : 0);
        ps.setInt(5, employee.getBankAccount());
        ps.setDouble(6, employee.getSalary());
        ps.setString(7, employee.getStartLocalDate().toString());
        ps.setString(8, employee.getEndLocalDate() == null
                ? null : employee.getEndLocalDate().toString());

        EmployeeTerms terms = employee.getTerms();
        ps.setString(9, terms.getJobStatus().name());
        ps.setString(10, terms.getSalaryType().name());
        ps.setInt(11, terms.getRestDays());
        ps.setString(12, terms.getDayOff().name());
    }

    /** Converts one result-set row into an Employee, resolving the branch from BranchRegistry. */
    private Employee mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        Branch branch = branchRegistry.getBranchByName(rs.getString("branch_name"));
        int bankAccount = rs.getInt("bank_account");
        double salary = rs.getDouble("salary");
        LocalDate startDate = LocalDate.parse(rs.getString("start_date"));

        EmployeeTerms terms = new EmployeeTerms(
                JobStatus.valueOf(rs.getString("job_status")),
                SalaryType.valueOf(rs.getString("salary_type")),
                rs.getInt("rest_days"),
                DayOfWeek.valueOf(rs.getString("day_off"))
        );

        Employee employee = new Employee(name, id, branch, bankAccount, salary, terms, startDate);

        employee.setManager(rs.getInt("is_manager") == 1);

        String endDate = rs.getString("end_date");
        if (endDate != null) {
            employee.setEndLocalDate(LocalDate.parse(endDate));
        }

        return employee;
    }

    // ------------------------------------------------------------------
    // role mapping (polymorphic StandardRole / DriverRole)
    // ------------------------------------------------------------------

    /** Queries employee_roles for the given employee and attaches each role to the Employee object. */
    private void loadRoles(Connection connection, Employee employee) throws SQLException {
        String sql = "SELECT role_kind, role_name, license FROM employee_roles WHERE employee_id = ?;";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, employee.getId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Role role = RoleMapper.resolve(
                            rs.getString("role_kind"),
                            rs.getString("role_name"),
                            rs.getString("license")
                    );
                    employee.addRole(role);
                }
            }
        }
    }

    /** Batch-inserts all of an employee's roles into employee_roles. */
    private void saveRoles(Connection connection, Employee employee) throws SQLException {
        String sql = """
                INSERT INTO employee_roles (employee_id, role_kind, role_name, license)
                VALUES (?, ?, ?, ?);
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Role role : employee.getRoles()) {
                ps.setInt(1, employee.getId());
                ps.setString(2, RoleMapper.kindOf(role));
                setNullableString(ps, 3, RoleMapper.roleNameOf(role));
                setNullableString(ps, 4, RoleMapper.licenseOf(role));
                ps.addBatch();
            }
            ps.executeBatch();
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

    /** Deletes all existing role rows for this employee, then inserts the current role set. */
    private void replaceRoles(Connection connection, Employee employee) throws SQLException {
        try (PreparedStatement del = connection.prepareStatement(
                "DELETE FROM employee_roles WHERE employee_id = ?;")) {
            del.setInt(1, employee.getId());
            del.executeUpdate();
        }
        saveRoles(connection, employee);
    }
}