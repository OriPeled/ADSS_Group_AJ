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

            createEmployee(employeeService, roleService, constraintService,
                    "Ronaldo", 7, Role.shiftManager);

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
                    "Avicay", 101, Role.Cashier);
            roleService.addRoleToEmployee(101, Role.Storekeeper);

// Fired employee
            createEmployee(employeeService, roleService, constraintService,
                    "Johnny Bravo", 77, Role.shiftManager);
            accessService.Register(77, "7777");
            employeeService.remove(77);

            // ============================
            // WEEK OF 19-25/04/2026 SHIFTS
            // ============================

            LocalDate date1 = LocalDate.of(2026,4,19);

            for (int i = 0; i < 7; i++) {

                LocalDate date11 = date1.plusDays(i);
                Shift pastShift1 = shiftService.getShift(date11, ShiftType.morning);

                shiftService.forceAssign(pastShift1, Role.shiftManager, 111);
                shiftService.forceAssign(pastShift1, Role.Cashier, 222);
                shiftService.forceAssign(pastShift1, Role.Cashier, 223);
                shiftService.forceAssign(pastShift1, Role.Cashier, 224);
                shiftService.forceAssign(pastShift1, Role.Storekeeper, 333);
                shiftService.forceAssign(pastShift1, Role.Storekeeper, 444);
                shiftService.forceAssign(pastShift1, Role.Storekeeper, 445);

                //shiftService.addShift(date1, ShiftType.evening);
                Shift pastShift2 = shiftService.getShift(date11, ShiftType.evening);

                shiftService.forceAssign(pastShift2, Role.shiftManager, 113);
                shiftService.forceAssign(pastShift2, Role.Cashier, 222);
                shiftService.forceAssign(pastShift2, Role.Cashier, 223);
                shiftService.forceAssign(pastShift2, Role.Cashier, 224);
                shiftService.forceAssign(pastShift2, Role.Storekeeper, 333);
                shiftService.forceAssign(pastShift2, Role.Storekeeper, 444);
                shiftService.forceAssign(pastShift2, Role.Storekeeper, 445);
            }
            shiftService.publishWeekByDate(date1);

            // =========================
            // CONSTRAINTS
            // =========================
            constraintService.update(222, DayOfWeek.WEDNESDAY, ShiftType.rest);
            constraintService.update(222, DayOfWeek.FRIDAY, ShiftType.rest);
            constraintService.update(111, DayOfWeek.SATURDAY, ShiftType.rest);
            constraintService.update(444, DayOfWeek.SATURDAY, ShiftType.rest);

            // ===========================
            // NEXT WEEK (26.4-2.5) SHIFTS
            // ===========================

            constraintService.update(111, DayOfWeek.SATURDAY, ShiftType.rest);

            LocalDate date2 = LocalDate.of(2026,4,26);

            for (int i = 0; i < 6; i++) {

                LocalDate date22 = date2.plusDays(i);
                Shift futureShift1 = shiftService.getShift(date22, ShiftType.morning);

                shiftService.assignEmployee(futureShift1, Role.shiftManager, 113);
                shiftService.assignEmployee(futureShift1, Role.Cashier, 7);
                shiftService.assignEmployee(futureShift1, Role.Cashier, 223);
                shiftService.assignEmployee(futureShift1, Role.Cashier, 224);
                shiftService.assignEmployee(futureShift1, Role.Storekeeper, 333);
                shiftService.assignEmployee(futureShift1, Role.Storekeeper, 444);
                shiftService.assignEmployee(futureShift1, Role.Storekeeper, 445);

                //shiftService.addShift(date1, ShiftType.evening);
                Shift futureShift2 = shiftService.getShift(date22, ShiftType.evening);

                shiftService.assignEmployee(futureShift2, Role.shiftManager, 113);
                shiftService.assignEmployee(futureShift2, Role.Cashier, 7);
                shiftService.assignEmployee(futureShift2, Role.Cashier, 223);
                shiftService.assignEmployee(futureShift2, Role.Cashier, 224);
                shiftService.assignEmployee(futureShift2, Role.Storekeeper, 333);
                shiftService.assignEmployee(futureShift2, Role.Storekeeper, 444);
                shiftService.assignEmployee(futureShift2, Role.Storekeeper, 445);
            }

            // SATURDAY
            for (int i = 6; i < 7; i++) {
                LocalDate date22 = date2.plusDays(i);

                Shift futureShift1 = shiftService.getShift(date22, ShiftType.morning);
                shiftService.assignEmployee(futureShift1, Role.shiftManager, 113);
                shiftService.assignEmployee(futureShift1, Role.Cashier, 7);
                shiftService.assignEmployee(futureShift1, Role.Cashier, 222);
                shiftService.assignEmployee(futureShift1, Role.Cashier, 223);
                shiftService.assignEmployee(futureShift1, Role.Storekeeper, 112);
                shiftService.assignEmployee(futureShift1, Role.Storekeeper, 333);
                shiftService.assignEmployee(futureShift1, Role.Storekeeper, 445);

                Shift futureShift2 = shiftService.getShift(date22, ShiftType.evening);
                shiftService.assignEmployee(futureShift2, Role.shiftManager, 113);
                shiftService.assignEmployee(futureShift2, Role.Cashier, 7);
                shiftService.assignEmployee(futureShift2, Role.Cashier, 223);
                shiftService.assignEmployee(futureShift2, Role.Storekeeper, 112);
                shiftService.assignEmployee(futureShift2, Role.Storekeeper, 333);
                shiftService.assignEmployee(futureShift2, Role.Storekeeper, 445);
            }

            // 1 assignment (Saturday evening) left

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