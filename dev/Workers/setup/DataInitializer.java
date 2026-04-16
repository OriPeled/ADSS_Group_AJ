package dev.Workers.setup;

import dev.Workers.Service.*;
import dev.Workers.domain.Enums.*;
import dev.Workers.domain.EmployeeTerms;
import dev.Workers.domain.Objects.Shift;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class DataInitializer {

    public static void initSystem() {
        // Only talk to the Service layer
        EmployeeService employeeService = EmployeeService.getInstance();
        RoleService roleService = RoleService.getInstance();
        AccessService accessService = AccessService.getInstance();
        ShiftService shiftService = ShiftService.getInstance();
        ConstraintService constraintService = ConstraintService.getInstance();

        System.out.println("Loading mock data into memory...");

        try {
            constraintService.setDeadline(LocalDate.now().plusWeeks(1));

            // Employee 1 — Shira
            EmployeeTerms terms1 = new EmployeeTerms(JobStatus.fullTime, SalaryType.global, 5);
            employeeService.add("Shira Steinbuch", 111, 100100, 10000.0, terms1, LocalDate.now().minusYears(1));
            accessService.Register(111, "1111");
            roleService.addRoleToEmployee(111, Role.shiftManager);
            constraintService.initConstraintForEmployee(111);   // new method we'll add
            accessService.login(111, "1111"); // Log in to initialize session for constraints
            // Employee 2 — Kokhava
            EmployeeTerms terms2 = new EmployeeTerms(JobStatus.halfTime, SalaryType.hourly, 3);
            employeeService.add("Kokhava Shavit", 222, 200200, 40.0, terms2, LocalDate.now().minusMonths(6));
            accessService.Register(222, "2222");
            roleService.addRoleToEmployee(222, Role.Cashier);
            constraintService.initConstraintForEmployee(222);
            accessService.login(222, "2222");

            EmployeeTerms termsNissim = new EmployeeTerms(JobStatus.fullTime, SalaryType.hourly, 30);
            employeeService.add("Nissim", 333, 300300, 32.0, termsNissim, LocalDate.now().minusYears(2));
            accessService.Register(333, "3333");
            roleService.addRoleToEmployee(333, Role.Storekeeper);
            constraintService.initConstraintForEmployee(333);
            accessService.login(333, "3333");

            EmployeeTerms termsRamzi = new EmployeeTerms(JobStatus.fullTime, SalaryType.global, 5);
            employeeService.add("Ramzi", 444, 444444, 8000.0, termsRamzi, LocalDate.now().minusYears(7));
            accessService.Register(444, "44444");
            roleService.addRoleToEmployee(444, Role.Storekeeper);
            constraintService.initConstraintForEmployee(444);


            // Set some constraints
            constraintService.update(111, DayOfWeek.SUNDAY, ShiftType.morning);
            constraintService.update(333, DayOfWeek.MONDAY, ShiftType.rest);
            constraintService.update(444, DayOfWeek.TUESDAY, ShiftType.evening);
            constraintService.update(222, DayOfWeek.WEDNESDAY, ShiftType.rest);

            // Create a shift and assign some employees
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            shiftService.addShift(tomorrow, ShiftType.morning);
            Shift morningShift = shiftService.getShift(tomorrow, ShiftType.morning);

            if (morningShift != null) {
                shiftService.setRequirement(morningShift, Role.shiftManager, 1);
                shiftService.setRequirement(morningShift, Role.Cashier, 2);
                shiftService.setRequirement(morningShift, Role.Storekeeper, 1);
                shiftService.assignEmployee(morningShift, Role.shiftManager, 111);
                shiftService.assignEmployee(morningShift, Role.Cashier, 222);
            }
            LocalDate baseDate = LocalDate.now().plusDays(1);
            shiftService.addShift(baseDate, ShiftType.morning);
            Shift s1 = shiftService.getShift(baseDate, ShiftType.morning);
            shiftService.setRequirement(s1, Role.shiftManager, 1);
            shiftService.setRequirement(s1, Role.Cashier, 1);
            shiftService.setRequirement(s1, Role.Storekeeper, 1);
            //shiftService.assignEmployee(s1, Role.shiftManager, 111); // Shira
            //shiftService.assignEmployee(s1, Role.Cashier, 222);      // Kokhava
            shiftService.assignEmployee(s1, Role.Storekeeper, 333);  // Nissim
            LocalDate day2 = baseDate.plusDays(1);
            shiftService.addShift(day2, ShiftType.evening);
            Shift s2 = shiftService.getShift(day2, ShiftType.evening);
            shiftService.setRequirement(s2, Role.shiftManager, 1);
            shiftService.setRequirement(s2, Role.Storekeeper, 1);
            shiftService.assignEmployee(s2, Role.shiftManager, 111);
            shiftService.assignEmployee(s2, Role.Storekeeper, 444); // Ramzi
            LocalDate day3 = baseDate.plusDays(2);
            shiftService.addShift(day3, ShiftType.morning);
            Shift s3 = shiftService.getShift(day3, ShiftType.morning);
            shiftService.setRequirement(s3, Role.Cashier, 1);
            shiftService.setRequirement(s3, Role.Storekeeper, 1);
            //shiftService.assignEmployee(s3, Role.Cashier, 222);
            //shiftService.assignEmployee(s3, Role.Storekeeper, 333);
            LocalDate day4 = baseDate.plusDays(3);
            shiftService.addShift(day4, ShiftType.evening);
            Shift s4 = shiftService.getShift(day4, ShiftType.evening);
            shiftService.setRequirement(s4, Role.shiftManager, 1);
            shiftService.assignEmployee(s4, Role.shiftManager, 111);
            System.out.println("Mock data loaded successfully!");

        } catch (Exception e) {
            System.out.println("Error loading mock data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}