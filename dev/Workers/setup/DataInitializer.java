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

            // Employee 2 — Kokhava
            EmployeeTerms terms2 = new EmployeeTerms(JobStatus.halfTime, SalaryType.hourly, 3);
            employeeService.add("Kokhava Shavit", 222, 200200, 40.0, terms2, LocalDate.now().minusMonths(6));
            accessService.Register(222, "2222");
            roleService.addRoleToEmployee(222, Role.Cashier);
            constraintService.initConstraintForEmployee(222);

            // ... and so on for 333, 444, 555

            // Set some constraints
            constraintService.update(111, DayOfWeek.SUNDAY, shiftType.morning);
            constraintService.update(444, DayOfWeek.MONDAY, shiftType.notWorking);
            constraintService.update(555, DayOfWeek.TUESDAY, shiftType.evening);
            constraintService.update(222, DayOfWeek.WEDNESDAY, shiftType.notWorking);

            // Create a shift and assign some employees
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            shiftService.addShift(tomorrow, shiftType.morning);
            Shift morningShift = shiftService.getShift(tomorrow, shiftType.morning);

            if (morningShift != null) {
                shiftService.setRequirement(morningShift, Role.shiftManager, 1);
                shiftService.setRequirement(morningShift, Role.Cashier, 2);
                shiftService.setRequirement(morningShift, Role.Storekeeper, 1);
                shiftService.assignEmployee(morningShift, Role.shiftManager, 111);
                shiftService.assignEmployee(morningShift, Role.Cashier, 555);
            }

            System.out.println("Mock data loaded successfully!");

        } catch (Exception e) {
            System.out.println("Error loading mock data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}