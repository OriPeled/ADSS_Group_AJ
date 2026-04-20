package dev.Workers.setup;

import dev.Workers.Service.*;
import dev.Workers.domain.Enums.*;
import dev.Workers.domain.Objects.EmployeeTerms;
import dev.Workers.domain.Objects.Shift;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * DataInitializer is responsible for loading mock data into the system.
 *
 * It creates:
 * - Employees with full details
 * - Roles and access credentials
 * - Constraints
 * - Smart assigned shifts for next week (Sunday–Saturday)
 *
 * The assignment is done automatically using the system logic,
 * so no crashes will occur due to constraints or invalid assignments.
 */
public class DataInitializer {

    public static void initSystem() {

        EmployeeService employeeService = EmployeeService.getInstance();
        RoleService roleService = RoleService.getInstance();
        AccessService accessService = AccessService.getInstance();
        ShiftService shiftService = ShiftService.getInstance();
        ConstraintService constraintService = ConstraintService.getInstance();

        try {
            // =========================
            // EMPLOYEES CREATION
            // =========================
// Shift Managers
            createEmployee(employeeService, roleService, constraintService,
                    "Shira Steinbuch", 111, Role.shiftManager);

            createEmployee(employeeService, roleService, constraintService,
                    "Daniel Cohen", 112, Role.shiftManager);

            createEmployee(employeeService, roleService, constraintService,
                    "Noa Levi", 113, Role.shiftManager);

// Cashiers
            createEmployee(employeeService, roleService, constraintService,
                    "Kokhava Shavit", 222, Role.Cashier);
            accessService.Register(222, "2222");

            createEmployee(employeeService, roleService, constraintService,
                    "Lior Mizrahi", 223, Role.Cashier);

            createEmployee(employeeService, roleService, constraintService,
                    "Dana Azulay", 224, Role.Cashier);

// Storekeepers
            createEmployee(employeeService, roleService, constraintService,
                    "Nissim", 333, Role.Storekeeper);

            createEmployee(employeeService, roleService, constraintService,
                    "Ramzi", 444, Role.Storekeeper);

            createEmployee(employeeService, roleService, constraintService,
                    "Eyal Peretz", 445, Role.Storekeeper);

// Mixed roles
            createEmployee(employeeService, roleService, constraintService,
                    "Ronaldo", 7, Role.shiftManager);

            createEmployee(employeeService, roleService, constraintService,
                    "Avicay", 101, Role.Cashier);

            // =========================
            // CONSTRAINTS
            // Set Saturday as the rest day for the listed employees so they are
            // unavailable for both morning and evening shifts on Saturdays.
            // ConstraintManager.update() uses Map.put() and is idempotent.
            // =========================
            constraintService.update(111, DayOfWeek.SATURDAY, ShiftType.rest);
            constraintService.update(112, DayOfWeek.SATURDAY, ShiftType.rest);
            constraintService.update(222, DayOfWeek.SATURDAY, ShiftType.rest);
            constraintService.update(223, DayOfWeek.SATURDAY, ShiftType.rest);
            constraintService.update(224, DayOfWeek.SATURDAY, ShiftType.rest);
            constraintService.update(444, DayOfWeek.SATURDAY, ShiftType.rest);
            constraintService.update(7,   DayOfWeek.SATURDAY, ShiftType.rest);

            Shift pastShift1 = shiftService.getShift(LocalDate.of(2025, 5, 20), ShiftType.morning);
            shiftService.forceAssign(pastShift1, Role.Cashier, 111);

            // =========================
            // NEXT WEEK SHIFTS
            // =========================
            LocalDate nextSunday = LocalDate.now()
                    .with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.SUNDAY));

            for (int i = 0; i < 5; i++) {
                LocalDate date = nextSunday.plusDays(i);
                shiftService.addShift(date, ShiftType.morning);
            }

            System.out.println("Mock data loaded successfully!");

        } catch (Exception e) {
            System.err.println("Failed to load mock data: " + e.getMessage());
        }
    }

    /**
     * Helper method to create an employee with full setup.
     */
    private static void createEmployee(
            EmployeeService employeeService,
            RoleService roleService,
            ConstraintService constraintService,
            String name,
            int id,
            Role role
    ) {
        EmployeeTerms terms = new EmployeeTerms(
                JobStatus.fullTime,
                SalaryType.global,
                5
        );
        employeeService.add(name, id, id * 100, 9000.0, terms, LocalDate.now().minusYears(1));
        roleService.addRoleToEmployee(id, role);
        constraintService.initConstraintForEmployee(id);
    }
}