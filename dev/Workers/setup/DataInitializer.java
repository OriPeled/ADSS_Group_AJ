package dev.Workers.setup;

import dev.Workers.Service.*;
import dev.Workers.domain.*;
import dev.Workers.domain.Enums.*;

import java.time.DayOfWeek;
import java.time.LocalDate;



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

            EmployeeTerms terms1 = new EmployeeTerms(JobStatus.fullTime, SalaryType.global, 5);
            employeeService.add("Shira Steinbuch", 111, 100100, 10000.0, terms1, LocalDate.now().minusYears(1));
            accessService.Register(111, "111");
            roleService.addRoleToEmployee(111, Role.shiftManager);
            constraintService.getEmployeeConstraints().put(111, new Constraint());

            EmployeeTerms terms2 = new EmployeeTerms(JobStatus.halfTime, SalaryType.hourly, 3);
            employeeService.add("Kokhava Shavit", 222, 200200, 40.0, terms2, LocalDate.now().minusMonths(6));
            accessService.Register(222, "222");
            roleService.addRoleToEmployee(222, Role.Cashier);
            constraintService.getEmployeeConstraints().put(222, new Constraint());

            EmployeeTerms terms3 = new EmployeeTerms(JobStatus.fullTime, SalaryType.hourly, 5);
            employeeService.add("Ramzi Abd Ramzi", 333, 300300, 45.0, terms3, LocalDate.now().minusYears(2));
            accessService.Register(333, "333");
            roleService.addRoleToEmployee(333, Role.Storekeeper);
            constraintService.getEmployeeConstraints().put(333, new Constraint());

            EmployeeTerms terms4 = new EmployeeTerms(JobStatus.halfTime, SalaryType.hourly, 4);
            employeeService.add("Avichai", 444, 400400, 50.0, terms4, LocalDate.now().minusMonths(2));
            accessService.Register(444, "444");
            roleService.addRoleToEmployee(444, Role.shiftManager);
            constraintService.getEmployeeConstraints().put(444, new Constraint());

            EmployeeTerms terms5 = new EmployeeTerms(JobStatus.fullTime, SalaryType.hourly, 5);
            employeeService.add("Esti", 555, 500500, 42.0, terms5, LocalDate.now().minusYears(3));
            accessService.Register(555, "555");
            roleService.addRoleToEmployee(555, Role.Cashier);
            constraintService.getEmployeeConstraints().put(555, new Constraint());


            constraintService.update(111, DayOfWeek.SUNDAY, shiftType.morning);

            constraintService.update(444, DayOfWeek.MONDAY, shiftType.notWorking);

            constraintService.update(555, DayOfWeek.TUESDAY, shiftType.evening);

            constraintService.update(222, DayOfWeek.WEDNESDAY, shiftType.notWorking);

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

            System.out.println("Mock data loaded successfully (including 5 employees and constraints)!");

        } catch (Exception e) {
            System.out.println("Error loading mock data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}