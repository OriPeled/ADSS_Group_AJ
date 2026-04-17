package dev.Workers.setup;

import dev.Workers.Service.*;
import dev.Workers.domain.Enums.*;
import dev.Workers.domain.EmployeeTerms;
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

        System.out.println("Loading mock data into memory...");

        try {

            constraintService.setDeadline(LocalDate.now().plusWeeks(1));

            // =========================
            // EMPLOYEES CREATION
            // =========================

            // Shift Managers
            createEmployee(employeeService, accessService, roleService, constraintService,
                    "Shira Steinbuch", 111, Role.shiftManager);

            createEmployee(employeeService, accessService, roleService, constraintService,
                    "Daniel Cohen", 112, Role.shiftManager);

            createEmployee(employeeService, accessService, roleService, constraintService,
                    "Noa Levi", 113, Role.shiftManager);

            // Cashiers
            createEmployee(employeeService, accessService, roleService, constraintService,
                    "Kokhava Shavit", 222, Role.Cashier);

            createEmployee(employeeService, accessService, roleService, constraintService,
                    "Lior Mizrahi", 223, Role.Cashier);

            createEmployee(employeeService, accessService, roleService, constraintService,
                    "Dana Azulay", 224, Role.Cashier);

            // Storekeepers
            createEmployee(employeeService, accessService, roleService, constraintService,
                    "Nissim", 333, Role.Storekeeper);

            createEmployee(employeeService, accessService, roleService, constraintService,
                    "Ramzi", 444, Role.Storekeeper);

            createEmployee(employeeService, accessService, roleService, constraintService,
                    "Eyal Peretz", 445, Role.Storekeeper);

            // =========================
            // CONSTRAINTS (Optional)
            // =========================
            // Example constraints (system will handle them safely)
           // constraintService.update(222, DayOfWeek.MONDAY, ShiftType.rest);
           // constraintService.update(111, DayOfWeek.SUNDAY, ShiftType.morning);

            // =========================
            // NEXT WEEK SHIFTS (SMART)
            // =========================

            LocalDate nextSunday = LocalDate.now()
                    .with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.SUNDAY));

            for (int i = 0; i < 7; i++) {

                LocalDate date = nextSunday.plusDays(i);

                Shift shift = shiftService.getShift(date, ShiftType.morning);

                // Set requirements
                shiftService.setRequirement(shift, Role.shiftManager, 3);
                shiftService.setRequirement(shift, Role.Cashier, 3);
                shiftService.setRequirement(shift, Role.Storekeeper, 3);
                shiftService.assignEmployee(shift, Role.shiftManager, 111);
                shiftService.assignEmployee(shift, Role.shiftManager, 112);
                shiftService.assignEmployee(shift, Role.shiftManager, 113);
                shiftService.assignEmployee(shift, Role.Cashier, 222);
                shiftService.assignEmployee(shift, Role.Cashier, 223);
                shiftService.assignEmployee(shift, Role.Cashier, 224);
                shiftService.assignEmployee(shift, Role.Storekeeper, 333);
                shiftService.assignEmployee(shift, Role.Storekeeper, 444);
                shiftService.assignEmployee(shift, Role.Storekeeper, 445);


            }

            System.out.println("Mock data loaded successfully!");

        } catch (Exception e) {
            System.out.println("Error loading mock data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper method to create an employee with full setup.
     */
    private static void createEmployee(
            EmployeeService employeeService,
            AccessService accessService,
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
        accessService.Register(id, String.valueOf(id));
        roleService.addRoleToEmployee(id, role);
        constraintService.initConstraintForEmployee(id);
    }
}