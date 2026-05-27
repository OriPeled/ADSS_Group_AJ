package dev.Workers.setup;

import dev.Workers.Service.*;
import dev.Workers.domain.Enums.*;
import dev.Workers.domain.Objects.EmployeeTerms;
import dev.Workers.domain.Objects.Role;
import dev.Workers.domain.Objects.Shift;
import dev.Workers.domain.Objects.StandardRole;
import dev.Workers.domain.RoleRegistry;

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
 * so no crashes will occur due to constraints or invalid assignments.*/


public class DataInitializer {
    public static void initSystem() {

        EmployeeService employeeService = EmployeeService.getInstance();
        RoleService roleService = RoleService.getInstance();
        AccessService accessService = AccessService.getInstance();
        ShiftService shiftService = ShiftService.getInstance();
        ConstraintService constraintService = ConstraintService.getInstance();

        RoleRegistry roleRegistry = RoleRegistry.getInstance();
        Role cashier = roleRegistry.getRoleByName("Cashier");
        Role storekeeper = roleRegistry.getRoleByName("Storekeeper");

        try {
            // =========================
            // EMPLOYEES CREATION
            // =========================
// Shift Managers
            createEmployee(employeeService, roleService, constraintService,
                    "Shira Steinbuch", 111, cashier);
            employeeService.getById(111).setManager(true);

            createEmployee(employeeService, roleService, constraintService,
                    "Daniel Cohen", 112, cashier);
            employeeService.getById(112).setManager(true);

            createEmployee(employeeService, roleService, constraintService,
                    "Noa Levi", 113, cashier);
            employeeService.getById(113).setManager(true);

            createEmployee(employeeService, roleService, constraintService,
                    "Ronaldo", 7, cashier);
            employeeService.getById(7).setManager(true);

// Cashiers
            createEmployee(employeeService, roleService, constraintService,
                    "Kokhava Shavit", 222, cashier);
            accessService.Register(222, "2222");

            createEmployee(employeeService, roleService, constraintService,
                    "Lior Mizrahi", 223, cashier);

            createEmployee(employeeService, roleService, constraintService,
                    "Dana Azulay", 224, cashier);

// Storekeepers
            createEmployee(employeeService, roleService, constraintService,
                    "Nissim", 333, storekeeper);

            createEmployee(employeeService, roleService, constraintService,
                    "Ramzi", 444, storekeeper);

            createEmployee(employeeService, roleService, constraintService,
                    "Eyal Peretz", 445, storekeeper);

// Mixed roles
            createEmployee(employeeService, roleService, constraintService,
                    "Avicay", 101, cashier);
            roleService.addRoleToEmployee(101, storekeeper);

// Fired employee
            createEmployee(employeeService, roleService, constraintService,
                    "Johnny Bravo", 77, storekeeper);
            accessService.Register(77, "7777");
            employeeService.fire(77);

            // ============================
            // WEEK OF 19-25/04/2026 SHIFTS
            // ============================

            LocalDate date1 = LocalDate.of(2026,4,19);

            for (int i = 0; i < 7; i++) {

                LocalDate date11 = date1.plusDays(i);
                Shift pastShift1 = shiftService.getShift(date11, ShiftType.MORNING);

                shiftService.forceAssign(pastShift1, cashier, 111);
                shiftService.forceAssign(pastShift1, cashier, 223);
                shiftService.forceAssign(pastShift1, cashier, 224);
                shiftService.forceAssign(pastShift1, storekeeper, 333);
                shiftService.forceAssign(pastShift1, storekeeper, 444);
                shiftService.forceAssign(pastShift1, storekeeper, 445);

                Shift pastShift2 = shiftService.getShift(date11, ShiftType.EVENING);

                shiftService.forceAssign(pastShift2, cashier, 113);
                shiftService.forceAssign(pastShift2, cashier, 223);
                shiftService.forceAssign(pastShift2, cashier, 224);
                shiftService.forceAssign(pastShift2, storekeeper, 333);
                shiftService.forceAssign(pastShift2, storekeeper, 444);
                shiftService.forceAssign(pastShift2, storekeeper, 445);
            }
            shiftService.publishWeekByDate(date1);

            // =========================
            // CONSTRAINTS
            // =========================
            constraintService.update(222, DayOfWeek.WEDNESDAY, ShiftType.REST);
            constraintService.update(222, DayOfWeek.FRIDAY, ShiftType.REST);

            constraintService.update(101, DayOfWeek.SUNDAY, ShiftType.REST);
            constraintService.update(77, DayOfWeek.SUNDAY, ShiftType.REST);
            //constraintService.update(444, DayOfWeek.SUNDAY, ShiftType.rest);

            // ===========================
            // NEXT WEEK (26.4-2.5) SHIFTS
            // ===========================

            LocalDate date2 = LocalDate.of(2026,5,03);

            for (int i = 0; i < 6; i++) {

                LocalDate date22 = date2.plusDays(i);
                Shift futureShift1 = shiftService.getShift(date22, ShiftType.MORNING);

                shiftService.assignEmployee(futureShift1, cashier, 7);
                shiftService.assignEmployee(futureShift1, cashier, 223);
                shiftService.assignEmployee(futureShift1, cashier, 224);
                shiftService.assignEmployee(futureShift1, storekeeper, 333);
                shiftService.assignEmployee(futureShift1, storekeeper, 444);
                shiftService.assignEmployee(futureShift1, storekeeper, 445);

                Shift futureShift2 = shiftService.getShift(date22, ShiftType.EVENING);

                shiftService.assignEmployee(futureShift2, cashier, 7);
                shiftService.assignEmployee(futureShift2, cashier, 223);
                shiftService.assignEmployee(futureShift2, cashier, 224);
                shiftService.assignEmployee(futureShift2, storekeeper, 333);
                shiftService.assignEmployee(futureShift2, storekeeper, 444);
                shiftService.assignEmployee(futureShift2, storekeeper, 445);
            }

            // SATURDAY
            /*for (int i = 6; i < 7; i++) {
                LocalDate date22 = date2.plusDays(i);

                Shift futureShift1 = shiftService.getShift(date22, ShiftType.morning);
                shiftService.assignEmployee(futureShift1, Role.Cashier, 222);
                shiftService.assignEmployee(futureShift1, Role.Cashier, 223);
                shiftService.assignEmployee(futureShift1, Role.Cashier, 224);
                shiftService.assignEmployee(futureShift1, Role.Storekeeper, 333);
                shiftService.assignEmployee(futureShift1, Role.Storekeeper, 444);
                shiftService.assignEmployee(futureShift1, Role.Storekeeper, 445);

                Shift futureShift2 = shiftService.getShift(date22, ShiftType.evening);
                shiftService.assignEmployee(futureShift2, Role.Cashier, 222);
                shiftService.assignEmployee(futureShift2, Role.Cashier, 223);
                shiftService.assignEmployee(futureShift2, Role.Cashier, 224);
                shiftService.assignEmployee(futureShift2, Role.Storekeeper, 333);
                shiftService.assignEmployee(futureShift2, Role.Storekeeper, 444);
                shiftService.assignEmployee(futureShift2, Role.Storekeeper, 445);
            }*/

            // 1 assignment (Saturday evening) left

            System.out.println("Mock data loaded successfully!");

        } catch (Exception e) {
            System.err.println("Failed to load mock data: " + e.getMessage());
        }
    }

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
                5,
                DayOfWeek.SATURDAY
        );
        employeeService.add(name, id, LicenseType.A,id * 100, 9000.0, terms, LocalDate.now().minusYears(1));
        roleService.addRoleToEmployee(id, role);
        constraintService.initConstraintForEmployee(id);

        constraintService.setDeadline(DayOfWeek.FRIDAY);
    }
}
