package dev.Workers.setup;

import dev.Workers.service.*;
import dev.Workers.domain.BranchRegistry;
import dev.Workers.domain.Enums.*;
import dev.Workers.domain.Objects.*;
import dev.Workers.domain.Objects.Role;
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

        dev.Workers.service.EmployeeService employeeService = dev.Workers.service.EmployeeService.getInstance();
        dev.Workers.service.AccessService accessService = dev.Workers.service.AccessService.getInstance();
        dev.Workers.service.ShiftService shiftService = dev.Workers.service.ShiftService.getInstance();
        PreferenceService preferenceService = PreferenceService.getInstance();

        BranchRegistry branchRegistry = BranchRegistry.getInstance();
        Branch beerSheva = branchRegistry.getBranchByName("Beer-Sheva");

        RoleRegistry roleRegistry = RoleRegistry.getInstance();
        Role cashier = roleRegistry.getRoleByName("Cashier");
        Role storekeeper = roleRegistry.getRoleByName("Storekeeper");

        try {
            // =========================
            // EMPLOYEES CREATION
            // =========================
// Shift Managers
            createEmployee(employeeService, preferenceService,
                    "Shira Steinbuch", 111, beerSheva, cashier);
            employeeService.getEmployee(111).setManager(true);

            createEmployee(employeeService, preferenceService,
                    "Daniel Cohen", 112, beerSheva, cashier);
            employeeService.getEmployee(112).setManager(true);

            createEmployee(employeeService, preferenceService,
                    "Noa Levi", 113, beerSheva, cashier);
            employeeService.getEmployee(113).setManager(true);

            createEmployee(employeeService, preferenceService,
                    "Ronaldo", 7, beerSheva, cashier);
            employeeService.getEmployee(7).setManager(true);

// Cashiers
            createEmployee(employeeService, preferenceService,
                    "Kokhava Shavit", 222, beerSheva, cashier);
            accessService.Register(222, "2222");

            createEmployee(employeeService, preferenceService,
                    "Lior Mizrahi", 223, beerSheva, cashier);

            createEmployee(employeeService, preferenceService,
                    "Dana Azulay", 224, beerSheva, cashier);

// Storekeepers
            createEmployee(employeeService, preferenceService,
                    "Nissim", 333, beerSheva, storekeeper);

            createEmployee(employeeService, preferenceService,
                    "Ramzi", 444, beerSheva, storekeeper);

            createEmployee(employeeService, preferenceService,
                    "Eyal Peretz", 445, beerSheva, storekeeper);

// Mixed roles
            createEmployee(employeeService, preferenceService,
                    "Avicay", 101, beerSheva, cashier);
            employeeService.addRole(101, storekeeper);

// Fired employee
            createEmployee(employeeService, preferenceService,
                    "Johnny Bravo", 77, beerSheva, storekeeper);
            accessService.Register(77, "7777");
            employeeService.fire(77);

            // ============================
            // WEEK OF 19-25/04/2026 SHIFTS
            // ============================

            LocalDate date1 = LocalDate.of(2026,4,19);

            for (int i = 0; i < 7; i++) {

                LocalDate date11 = date1.plusDays(i);
                Shift pastShift1 = shiftService.getShift(beerSheva, date11, ShiftType.MORNING);

                shiftService.forceAssign(pastShift1, cashier, 111);
                shiftService.forceAssign(pastShift1, cashier, 223);
                shiftService.forceAssign(pastShift1, cashier, 224);
                shiftService.forceAssign(pastShift1, storekeeper, 333);
                shiftService.forceAssign(pastShift1, storekeeper, 444);
                shiftService.forceAssign(pastShift1, storekeeper, 445);

                Shift pastShift2 = shiftService.getShift(beerSheva, date11, ShiftType.EVENING);

                shiftService.forceAssign(pastShift2, cashier, 113);
                shiftService.forceAssign(pastShift2, cashier, 223);
                shiftService.forceAssign(pastShift2, cashier, 224);
                shiftService.forceAssign(pastShift2, storekeeper, 333);
                shiftService.forceAssign(pastShift2, storekeeper, 444);
                shiftService.forceAssign(pastShift2, storekeeper, 445);
            }
            shiftService.publishWeekByDate(beerSheva, date1);

            // =========================
            // CONSTRAINTS
            // =========================
            preferenceService.update(222, DayOfWeek.WEDNESDAY, ShiftType.REST);
            preferenceService.update(222, DayOfWeek.FRIDAY, ShiftType.REST);

            preferenceService.update(101, DayOfWeek.SUNDAY, ShiftType.REST);
            preferenceService.update(77, DayOfWeek.SUNDAY, ShiftType.REST);
            //constraintService.update(444, DayOfWeek.SUNDAY, ShiftType.rest);

            // ===========================
            // NEXT WEEK (26.4-2.5) SHIFTS
            // ===========================

            LocalDate date2 = LocalDate.of(2026,5,03);

            for (int i = 0; i < 6; i++) {

                LocalDate date22 = date2.plusDays(i);
                Shift futureShift1 = shiftService.getShift(beerSheva, date22, ShiftType.MORNING);

                shiftService.assignEmployee(futureShift1, cashier, 7);
                shiftService.assignEmployee(futureShift1, cashier, 223);
                shiftService.assignEmployee(futureShift1, cashier, 224);
                shiftService.assignEmployee(futureShift1, storekeeper, 333);
                shiftService.assignEmployee(futureShift1, storekeeper, 444);
                shiftService.assignEmployee(futureShift1, storekeeper, 445);

                Shift futureShift2 = shiftService.getShift(beerSheva, date22, ShiftType.EVENING);

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
            dev.Workers.service.EmployeeService employeeService,
            PreferenceService preferenceService,
            String name,
            int id,
            Branch branch,
            Role role
    ) {
        EmployeeTerms terms = new EmployeeTerms(
                JobStatus.fullTime,
                SalaryType.global,
                5,
                DayOfWeek.SATURDAY
        );
        employeeService.add(name, id, branch, id * 100, 9000.0, terms, LocalDate.now().minusYears(1));
        employeeService.addRole(id, role);
        preferenceService.initPreferences(id);

        preferenceService.setDeadline(DayOfWeek.FRIDAY);
    }
}
