package dev.Workers.setup;

import dev.Workers.service.*;
import dev.Workers.domain.BranchRegistry;
import dev.Workers.domain.Enums.*;
import dev.Workers.domain.Objects.*;
import dev.Workers.domain.Objects.Role;
import dev.Workers.domain.RoleRegistry;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

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
        RequirementService requirementService = RequirementService.getInstance();
        PreferenceService preferenceService = PreferenceService.getInstance();
        BranchRegistry branchRegistry = BranchRegistry.getInstance();

        try (java.sql.Connection connection = dev.Workers.database.DatabaseManager.getConnection();
             java.sql.Statement statement = connection.createStatement()) {

            statement.execute("INSERT OR IGNORE INTO branches (branch_name) VALUES ('Beer-Sheva'), ('Dimona'), ('Ofakim'), ('Rahat');");

            try (java.sql.ResultSet rs = statement.executeQuery("SELECT branch_name FROM branches;")) {
                while (rs.next()) {
                    String branchName = rs.getString("branch_name");
                    branchRegistry.registerBranch(new Branch(branchName));
                }
            }

        } catch (java.sql.SQLException e) {
            System.err.println("Failed to setup demo branches: " + e.getMessage());
            return;
        }

        Branch beerSheva = branchRegistry.getBranchByName("Beer-Sheva");

        RoleRegistry roleRegistry = RoleRegistry.getInstance();
        Role cashier = roleRegistry.getRoleByName("Cashier");
        Role storekeeper = roleRegistry.getRoleByName("Storekeeper");
        Role driverA = roleRegistry.getAllRoles().get(2);
        Role driverB = roleRegistry.getAllRoles().get(3);
        Role driverC = roleRegistry.getAllRoles().get(4);
        Role driverD = roleRegistry.getAllRoles().get(5);

        try {
            // =========================
            // EMPLOYEES CREATION
            // =========================
// Shift Managers
            createEmployee(employeeService, preferenceService,
                    "Shira Steinbuch", 111, beerSheva, cashier);
            //employeeService.getEmployee(111).setManager(true);
            employeeService.promoteDemote(111);

            createEmployee(employeeService, preferenceService,
                    "Daniel Cohen", 112, beerSheva, cashier);
            employeeService.promoteDemote(112);

            createEmployee(employeeService, preferenceService,
                    "Noa Levi", 113, beerSheva, cashier);
            employeeService.promoteDemote(113);

            createEmployee(employeeService, preferenceService,
                    "Ronaldo", 7, beerSheva, cashier);
            employeeService.promoteDemote(7);

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

// Drivers
            createEmployee(employeeService, preferenceService,
                    "Dominic Torretto", 501, beerSheva, driverD);
            employeeService.addRole(501, driverC);
            employeeService.addRole(501, driverB);
            employeeService.addRole(501, driverA);

            createEmployee(employeeService, preferenceService,
                    "Brian O'Conner", 502, beerSheva, driverC);
            employeeService.addRole(502, driverB);
            employeeService.addRole(502, driverA);

            createEmployee(employeeService, preferenceService,
                    "Letty Ortiz", 503, beerSheva, driverB);
            employeeService.addRole(503, driverA);

            createEmployee(employeeService, preferenceService,
                    "Frank Martin", 504, beerSheva, driverC);
            employeeService.addRole(504, driverB);
            employeeService.addRole(504, driverA);

            createEmployee(employeeService, preferenceService,
                    "Max Rockatansky", 505, beerSheva, driverD);
            employeeService.addRole(505, driverC);
            employeeService.addRole(505, driverA);

            createEmployee(employeeService, preferenceService,
                    "Baby", 506, beerSheva, driverB);
            employeeService.addRole(506, driverA);

            createEmployee(employeeService, preferenceService,
                    "Jason Bourne", 507, beerSheva, driverA);

// Mixed roles
            createEmployee(employeeService, preferenceService,
                    "Avicay", 101, beerSheva, cashier);
            employeeService.addRole(101, storekeeper);

// Fired employee
            createEmployee(employeeService, preferenceService,
                    "Johnny Bravo", 77, beerSheva, storekeeper);
            accessService.Register(77, "7777");
            //employeeService.fire(77);
            employeeService.fireRehire(77, LocalDate.now().minusDays(8));

            // ============================
            // WEEK OF 19-25/04/2026 SHIFTS
            // ============================

            LocalDate sunday1 = LocalDate.of(2026,4,19);
            //shiftService.initShiftsWeek(beerSheva);
            //requirementService.getDriverReqs(beerSheva);
            //requirementService.getStoreKeeperReqs(beerSheva);

            for (int i = 0; i < 7; i++) {

                LocalDate weekday1 = sunday1.plusDays(i);
                //shiftService.addShift(beerSheva, date11, ShiftType.MORNING);
                Shift pastShift1 = shiftService.getShift(beerSheva, weekday1, ShiftType.MORNING);
                shiftService.setRequirementManually(pastShift1, cashier, 3);
                shiftService.setRequirementManually(pastShift1, storekeeper, 3);

                shiftService.manualAssign(pastShift1, cashier, 111);
                shiftService.manualAssign(pastShift1, cashier, 223);
                shiftService.manualAssign(pastShift1, cashier, 224);
                shiftService.manualAssign(pastShift1, storekeeper, 333);
                shiftService.manualAssign(pastShift1, storekeeper, 444);
                shiftService.manualAssign(pastShift1, storekeeper, 445);

                //shiftService.addShift(beerSheva, date11, ShiftType.EVENING);
                Shift pastShift2 = shiftService.getShift(beerSheva, weekday1, ShiftType.EVENING);
                shiftService.setRequirementManually(pastShift2, cashier, 3);
                shiftService.setRequirementManually(pastShift2, storekeeper, 3);

                shiftService.manualAssign(pastShift2, cashier, 113);
                shiftService.manualAssign(pastShift2, cashier, 223);
                shiftService.manualAssign(pastShift2, cashier, 224);
                shiftService.manualAssign(pastShift2, storekeeper, 333);
                shiftService.manualAssign(pastShift2, storekeeper, 444);
                shiftService.manualAssign(pastShift2, storekeeper, 445);
            }
            shiftService.publishWeekByDate(beerSheva, sunday1);

            // ===========================
            // THIS' WEEK'S SHIFTS
            // ===========================

            LocalDate sunday2 = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

            for (int i = 0; i < 7; i++) {

                LocalDate weekday2 = sunday2.plusDays(i);
                //shiftService.addShift(beerSheva, date22, ShiftType.MORNING);
                Shift futureShift1 = shiftService.getShift(beerSheva, weekday2, ShiftType.MORNING);
                shiftService.setRequirementManually(futureShift1, cashier, 3);
                shiftService.setRequirementManually(futureShift1, storekeeper, 3);

                shiftService.manualAssign(futureShift1, cashier, 7);
                shiftService.manualAssign(futureShift1, cashier, 223);
                shiftService.manualAssign(futureShift1, cashier, 224);
                shiftService.manualAssign(futureShift1, storekeeper, 333);
                shiftService.manualAssign(futureShift1, storekeeper, 444);
                shiftService.manualAssign(futureShift1, storekeeper, 445);

                //shiftService.addShift(beerSheva, date22, ShiftType.EVENING);
                Shift futureShift2 = shiftService.getShift(beerSheva, weekday2, ShiftType.EVENING);
                shiftService.setRequirementManually(futureShift2, cashier, 3);
                shiftService.setRequirementManually(futureShift2, storekeeper, 3);

                shiftService.manualAssign(futureShift2, cashier, 7);
                shiftService.manualAssign(futureShift2, cashier, 223);
                shiftService.manualAssign(futureShift2, cashier, 224);
                shiftService.manualAssign(futureShift2, storekeeper, 333);
                shiftService.manualAssign(futureShift2, storekeeper, 444);
                shiftService.manualAssign(futureShift2, storekeeper, 445);
            }
            shiftService.publishWeekByDate(beerSheva, sunday2);

            // ===========================
            // NEXT WEEK SHIFTS
            // ===========================

            // =========================
// CONSTRAINTS
// =========================
            preferenceService.manualUpdate(222, DayOfWeek.WEDNESDAY, ShiftType.REST);
            preferenceService.manualUpdate(222, DayOfWeek.FRIDAY, ShiftType.REST);

            preferenceService.manualUpdate(101, DayOfWeek.SUNDAY, ShiftType.REST);
            preferenceService.manualUpdate(77, DayOfWeek.SUNDAY, ShiftType.REST);
            preferenceService.manualUpdate(7, DayOfWeek.SATURDAY, ShiftType.EVENING);

            LocalDate sunday3 = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));

            // SUNDAY TO FRIDAY
            for (int i = 0; i < 6; i++) {

                LocalDate weekday3 = sunday3.plusDays(i);

                // MORNING
                Shift futureShift1 = shiftService.getShift(beerSheva, weekday3, ShiftType.MORNING);
                shiftService.setRequirementManually(futureShift1, cashier, 3);
                shiftService.setRequirementManually(futureShift1, storekeeper, 3);
                shiftService.setRequirementManually(futureShift1, driverD, 1);
                shiftService.setRequirementManually(futureShift1, driverC, 1);

                shiftService.manualAssign(futureShift1, cashier, 7);
                shiftService.manualAssign(futureShift1, cashier, 223);
                shiftService.manualAssign(futureShift1, cashier, 224);
                shiftService.manualAssign(futureShift1, storekeeper, 333);
                shiftService.manualAssign(futureShift1, storekeeper, 444);
                shiftService.manualAssign(futureShift1, storekeeper, 445);
                shiftService.manualAssign(futureShift1, driverD, 501);
                shiftService.manualAssign(futureShift1, driverC, 502);

                // EVENING
                Shift futureShift2 = shiftService.getShift(beerSheva, weekday3, ShiftType.EVENING);
                shiftService.setRequirementManually(futureShift2, cashier, 3);
                shiftService.setRequirementManually(futureShift2, storekeeper, 3);
                shiftService.setRequirementManually(futureShift2, driverB, 1);
                shiftService.setRequirementManually(futureShift2, driverC, 1);

                shiftService.assignEmployee(futureShift2, cashier, 7);
                shiftService.assignEmployee(futureShift2, cashier, 223);
                shiftService.assignEmployee(futureShift2, cashier, 224);
                shiftService.assignEmployee(futureShift2, storekeeper, 333);
                shiftService.assignEmployee(futureShift2, storekeeper, 444);
                shiftService.assignEmployee(futureShift2, storekeeper, 445);
                shiftService.assignEmployee(futureShift2, driverB, 503);
                shiftService.assignEmployee(futureShift2, driverC, 504);
            }

            // SATURDAY - 2 assignments (Saturday morning, evening) left
            for (int i = 6; i < 7; i++) {
                LocalDate s = sunday3.plusDays(i);

                // MORNING: 1 Cashier short, nobody available
                Shift futureShift1 = shiftService.getShift(beerSheva, s, ShiftType.MORNING);
                shiftService.setRequirementManually(futureShift1, cashier, 3);
                shiftService.setRequirementManually(futureShift1, storekeeper, 3);
                shiftService.setRequirementManually(futureShift1, driverD, 1);

                shiftService.manualAssign(futureShift1, cashier, 222);
                shiftService.manualAssign(futureShift1, cashier, 223);
                // shiftService.assignEmployee(futureShift1, cashier, 224); // <-- Missing 1 cashier
                shiftService.manualAssign(futureShift1, storekeeper, 333);
                shiftService.manualAssign(futureShift1, storekeeper, 444);
                shiftService.manualAssign(futureShift1, storekeeper, 445);
                shiftService.manualAssign(futureShift1, driverD, 505);

                // EVENING: 1 Storekeeper short
                Shift futureShift2 = shiftService.getShift(beerSheva, s, ShiftType.EVENING);
                shiftService.setRequirementManually(futureShift2, cashier, 3);
                shiftService.setRequirementManually(futureShift2, storekeeper, 3);
                shiftService.setRequirementManually(futureShift2, driverA, 1);

                shiftService.manualAssign(futureShift2, cashier, 222);
                shiftService.manualAssign(futureShift2, cashier, 223);
                shiftService.manualAssign(futureShift2, cashier, 224);
                shiftService.manualAssign(futureShift2, storekeeper, 333);
                shiftService.manualAssign(futureShift2, storekeeper, 444);
                // shiftService.assignEmployee(futureShift2, storekeeper, 445); // <-- Missing 1 storekeeper
                shiftService.manualAssign(futureShift2, driverA, 507);
            }

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

        //preferenceService.setDeadline(DayOfWeek.THURSDAY);
    }
}
