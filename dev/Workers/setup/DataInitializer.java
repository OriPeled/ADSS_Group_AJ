package dev.Workers.setup;

import dev.Workers.Service.*;
import dev.Workers.domain.Enums.*;
import dev.Workers.domain.EmployeeTerms;
import dev.Workers.domain.Objects.Shift;

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
            //constraintService.setDeadline(LocalDate.now().plusWeeks(1));
            //constraintService.setThisThursdayDeadline();
            //constraintService.setDeadline(DayOfWeek.THURSDAY);

            // =========================
            // EMPLOYEES CREATION
            // =========================

            // Shift Managers
            createEmployee(employeeService, accessService, roleService, constraintService,
                    "Shira Steinbuch", 111, Role.shiftManager, LocalDate.of(2023, 1, 10));

            createEmployee(employeeService, accessService, roleService, constraintService,
                    "Daniel Cohen", 112, Role.shiftManager, LocalDate.of(2022, 6, 15));

            createEmployee(employeeService, accessService, roleService, constraintService,
                    "Noa Levi", 113, Role.shiftManager, LocalDate.of(2024, 3, 5));

// Cashiers
            createEmployee(employeeService, accessService, roleService, constraintService,
                    "Kokhava Shavit", 222, Role.Cashier, LocalDate.of(2023, 11, 20));
            accessService.Register(222, "2222");

            createEmployee(employeeService, accessService, roleService, constraintService,
                    "Lior Mizrahi", 223, Role.Cashier, LocalDate.of(2024, 1, 12));

            createEmployee(employeeService, accessService, roleService, constraintService,
                    "Dana Azulay", 224, Role.Cashier, LocalDate.of(2025, 2, 1));

            // Storekeepers
            createEmployee(employeeService, accessService, roleService, constraintService,
                    "Nissim", 333, Role.Storekeeper, LocalDate.of(2021, 9, 30));

            createEmployee(employeeService, accessService, roleService, constraintService,
                    "Ramzi", 444, Role.Storekeeper, LocalDate.of(2022, 12, 25));

            createEmployee(employeeService, accessService, roleService, constraintService,
                    "Eyal Peretz", 445, Role.Storekeeper, LocalDate.of(2023, 7, 18));

// Mixed roles
            createEmployee(employeeService, accessService, roleService, constraintService,
                    "Ronaldo", 7, Role.shiftManager, LocalDate.of(2020, 5, 5));

            createEmployee(employeeService, accessService, roleService, constraintService,
                    "Avicay", 101, Role.Cashier, LocalDate.of(2025, 4, 15));
            // =========================
            // CONSTRAINTS =
            // =========================
            // Example constraints (system will handle them safely)
            //constraintService.update(222, DayOfWeek.SATURDAY, ShiftType.rest);
            constraintService.update(223, DayOfWeek.SATURDAY, ShiftType.rest);
            constraintService.update(7, DayOfWeek.SATURDAY, ShiftType.rest);
            constraintService.update(222, DayOfWeek.SATURDAY, ShiftType.rest);
            constraintService.update(444, DayOfWeek.SATURDAY, ShiftType.rest);
            constraintService.update(112, DayOfWeek.SATURDAY, ShiftType.rest);
            constraintService.update(224, DayOfWeek.SATURDAY, ShiftType.rest);
            constraintService.update(223, DayOfWeek.SATURDAY, ShiftType.rest);
            constraintService.update(111, DayOfWeek.SATURDAY, ShiftType.rest);
           // constraintService.update(111, DayOfWeek.SUNDAY, ShiftType.morning);
            // =========================
            // LAST WEEK SHIFTS 12/04/2026
            // =========================

            /*LocalDate lastSunday = LocalDate.now()
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
                    .minusWeeks(1);

            for (int i = 0; i < 7; i++) {

                LocalDate date = lastSunday.plusDays(i);

                shiftService.addShift(date, ShiftType.morning);
                Shift morningShift = shiftService.getShift(date, ShiftType.morning);

                // Set requirements for morning shift
                shiftService.setRequirement(morningShift, Role.shiftManager, 3);
                shiftService.setRequirement(morningShift, Role.Cashier, 3);
                shiftService.setRequirement(morningShift, Role.Storekeeper, 3);

                shiftService.assignEmployee(morningShift, Role.shiftManager, 111);
                shiftService.assignEmployee(morningShift, Role.shiftManager, 112);
                shiftService.assignEmployee(morningShift, Role.shiftManager, 113);
                shiftService.assignEmployee(morningShift, Role.Cashier, 222);
                shiftService.assignEmployee(morningShift, Role.Cashier, 223);
                shiftService.assignEmployee(morningShift, Role.Cashier, 224);
                shiftService.assignEmployee(morningShift, Role.Storekeeper, 333);
                shiftService.assignEmployee(morningShift, Role.Storekeeper, 444);
                shiftService.assignEmployee(morningShift, Role.Storekeeper, 445);

                shiftService.addShift(date, ShiftType.evening);
                Shift eveningShift = shiftService.getShift(date, ShiftType.evening);

                // Set requirements for evening shift
                shiftService.setRequirement(eveningShift, Role.shiftManager, 3);
                shiftService.setRequirement(eveningShift, Role.Cashier, 3);
                shiftService.setRequirement(eveningShift, Role.Storekeeper, 3);

                shiftService.assignEmployee(eveningShift, Role.shiftManager, 111);
                shiftService.assignEmployee(eveningShift, Role.shiftManager, 112);
                shiftService.assignEmployee(eveningShift, Role.shiftManager, 113);
                shiftService.assignEmployee(eveningShift, Role.Cashier, 222);
                shiftService.assignEmployee(eveningShift, Role.Cashier, 223);
                shiftService.assignEmployee(eveningShift, Role.Cashier, 224);
                shiftService.assignEmployee(eveningShift, Role.Storekeeper, 333);
                shiftService.assignEmployee(eveningShift, Role.Storekeeper, 444);
                shiftService.assignEmployee(eveningShift, Role.Storekeeper, 445);
            }
            shiftService.publishLastWeekSchedule();*/

            // =========================
            // This WEEK SHIFTS (SMART) 19/04/2026
            // =========================

            /*LocalDate thisSunday = LocalDate.now()
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

            for (int i = 0; i < 7; i++) {

                LocalDate date = thisSunday.plusDays(i);

                shiftService.addShift(date, ShiftType.morning);
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


                shiftService.addShift(date, ShiftType.evening);
                Shift eveningShift = shiftService.getShift(date, ShiftType.evening);

                shiftService.setRequirement(eveningShift, Role.shiftManager, 3);
                shiftService.setRequirement(eveningShift, Role.Cashier, 3);
                shiftService.setRequirement(eveningShift, Role.Storekeeper, 3);
                shiftService.assignEmployee(eveningShift, Role.shiftManager, 111);
                shiftService.assignEmployee(eveningShift, Role.shiftManager, 112);
                shiftService.assignEmployee(eveningShift, Role.shiftManager, 113);
                shiftService.assignEmployee(eveningShift, Role.Cashier, 222);
                shiftService.assignEmployee(eveningShift, Role.Cashier, 223);
                shiftService.assignEmployee(eveningShift, Role.Cashier, 224);
                shiftService.assignEmployee(eveningShift, Role.Storekeeper, 333);
                shiftService.assignEmployee(eveningShift, Role.Storekeeper, 444);
                shiftService.assignEmployee(eveningShift, Role.Storekeeper, 445);


            }
            shiftService.publishWeekSchedule();*/

            // =========================
            // NEXT WEEK SHIFTS 26/04/2026
            // =========================

            LocalDate nextSunday = LocalDate.now()
                    .with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.SUNDAY));

            /*for (int i = 0; i < 7; i++) {

                LocalDate date = nextSunday.plusDays(i);

                shiftService.addShift(date, ShiftType.morning);
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


                shiftService.addShift(date, ShiftType.evening);
                Shift eveningShift = shiftService.getShift(date, ShiftType.evening);

                shiftService.setRequirement(eveningShift, Role.shiftManager, 3);
                shiftService.setRequirement(eveningShift, Role.Cashier, 3);
                shiftService.setRequirement(eveningShift, Role.Storekeeper, 3);
                shiftService.assignEmployee(eveningShift, Role.shiftManager, 111);
                shiftService.assignEmployee(eveningShift, Role.shiftManager, 112);
                shiftService.assignEmployee(eveningShift, Role.shiftManager, 113);
                shiftService.assignEmployee(eveningShift, Role.Cashier, 222);
                shiftService.assignEmployee(eveningShift, Role.Cashier, 223);
                shiftService.assignEmployee(eveningShift, Role.Cashier, 224);
                shiftService.assignEmployee(eveningShift, Role.Storekeeper, 333);
                shiftService.assignEmployee(eveningShift, Role.Storekeeper, 444);
                shiftService.assignEmployee(eveningShift, Role.Storekeeper, 445);


            }*/
            //shiftService.publishNextWeekSchedule();
            for (int i = 0; i < 5; i++) {

                LocalDate date = nextSunday.plusDays(i);

                shiftService.addShift(date, ShiftType.morning);
                Shift shift = shiftService.getShift(date, ShiftType.morning);

                //shiftService.assignEmployee(shift, Role.Cashier, 222);

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
            Role role,
            LocalDate startDate
    ) {

        EmployeeTerms terms = new EmployeeTerms(
                JobStatus.fullTime,
                SalaryType.global,
                5
        );

        employeeService.add(name, id, id * 100, 9000.0, terms, LocalDate.now().minusYears(1));
        //accessService.Register(id, String.format("%04d", id));
        roleService.addRoleToEmployee(id, role);
        constraintService.initConstraintForEmployee(id);
    }
}