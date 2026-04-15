package dev.Workers.presentation;

import dev.Workers.Service.ConstraintManager;
import dev.Workers.Service.EmployeeManager;
import dev.Workers.Service.RoleManager;
import dev.Workers.domain.Enums.JobStatus;
import dev.Workers.domain.Enums.Role;
import dev.Workers.domain.Enums.SalaryType;
import dev.Workers.domain.Constraint;
import dev.Workers.domain.Employee;
import dev.Workers.domain.EmployeeTerms;

import java.time.LocalDate;
import java.util.Scanner;

import static dev.Workers.presentation.Main.scanner;

public class ManageEmployeesMenu {
    static EmployeeManager employeeManager = EmployeeManager.getInstance();
    static ConstraintManager constraintManager = ConstraintManager.getInstance();
    static RoleManager roleManager = RoleManager.getInstance();
    static int id;
    static Employee employee;

    public static void start() {
        System.out.println("Employees");
        System.out.println("1. Manage existing Employee");
        System.out.println("2. Add Employee");
        System.out.println("3. Back");

        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
               accessEmployee();
            case 2:
               addEmployeeMenu();
            case 3:
                AdminMode.start();
            default:
                System.out.println("Invalid choice.");
        }
    }

    private static void accessEmployee() {
        System.out.println("Manage Employee");
        System.out.println("Enter employee ID or enter 0 to go back:");
        id = scanner.nextInt();
        if (id == 0)
            start();

        employee = employeeManager.getById(id);
        System.out.println("Employee chosen");
        manageEmployee();
    }

    public static void manageEmployee() {
        System.out.println(employee.getName() + " (" + id + ")");
        /*System.out.println("1. Update Constraints");*/
        System.out.println("2. Employee Details");
        System.out.println("3. Promote/Demote");
        System.out.println("4. Remove");
        System.out.println("5. Add Role");
        System.out.println("6. Back");

        int choice = scanner.nextInt();
        switch (choice) {
            case 2:
                details();
            case 3:
                promoteDemote();
            case 4:
                remove();
            case 5:
                addRole();
            case 6:
                accessEmployee();
            default:
                System.out.println("Invalid choice.");
        }
    }

/*    private static void updateConstraints() {
        System.out.print("Update Constraints");
        System.out.print("Enter date (dd/mm/yyyy) or 0 to go back:");
        int input = scanner.nextInt();
        if (input == 0)
            manageEmployee();
        String dateString = String.valueOf(input);
        LocalDate date = Parser.stringToDate(dateString);

        System.out.print("Enter type (morning/evening):");
        String shiftTypeString = scanner.nextLine();

        Constraint constraint = new Constraint(date, shiftTypeString);
        constraintManager.addSingleItem(id, constraint);
        System.out.print("Constraints updated.");

        System.out.print("Enter 1 to add another constraint or 0 to go back.");
        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                updateConstraints();
            case 2:
                manageEmployee();
        }
    }*/

    private static void details() {
        System.out.print(employee.toString());
        System.out.print("Choose 1-4 to update detail or 0 to go back:");
        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                System.out.print("Enter new name:");
                String newName = scanner.nextLine();
                employee.setName(newName);
                System.out.println("Name updated.");
                details();
            case 2:
                System.out.println("Enter new bank account:");
                int newBankAccount = scanner.nextInt();
                employee.setBankAccount(newBankAccount);
                System.out.println("Bank account updated.");
                details();
            case 3:
                System.out.println("Enter new salary:");
                double newSalary = scanner.nextDouble();
                employee.setSalary(newSalary);
                System.out.println("Salary updated.");
                details();
            case 4:
                updateTerms();
            case 0:
                manageEmployee();
            default:
                System.out.println("Invalid choice.");
                details();
        }
    }

    public static void updateTerms() {
        EmployeeTerms terms = employee.getTerms();
        System.out.println(terms);
        System.out.println("Choose 1-3 to update detail or 0 to go back:");
        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                System.out.println("Enter 1 to change job status, 0 to cancel:");
                choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        terms.changeJobStatus();
                        System.out.println("Job Status is now" + terms.getJobStatus());
                        updateTerms();
                    case 0:
                        updateTerms();
                }
            case 2:
                System.out.println("Enter 1 to change salary type, 0 to cancel:");
                choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        terms.changeSalaryType();
                        System.out.println("Salary Type is now" + terms.getSalaryType());
                        updateTerms();
                    case 0:
                        updateTerms();
                }
            case 3:
                System.out.println("Enter number of rest days (1-7) or 0 to cancel:");
                choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        terms.setRestDays(choice);
                        System.out.println("Number of Rest Days is now" + terms.getRestDays());
                        updateTerms();
                    case 0:
                        updateTerms();
                }
            case 0:
                details();
        }
    }

    private static void promoteDemote() {
        employee.promoteDemote();
    }

    private static void remove() {
        System.out.println("Are you sure you want to remove " + employee.getName() + " (" + id + ")?" +
                           "If yes - enter 1, else 0.");

        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                employeeManager.remove(id);
                System.out.println("Employee removed.");
                accessEmployee();
                break;
            case 2:
                accessEmployee();
        }
    }
    /**
     * Adds a role to an employee after validating input.
     */
    private static void addRole() {
        Role[] roles = Role.values();

        System.out.println("Choose role to add:");
        int choice = scanner.nextInt();
        for (int i = 0; i < roles.length; i++) {
            System.out.println((i + 1) + ". " + roles[i]);
        }
        if (choice < 1 || choice > roles.length) {
            System.out.println("Invalid choice.");
            return;
        }
        Role selectedRole = roles[choice - 1];

        try {
            roleManager.addSingleItem(employee.getId(), selectedRole);
            System.out.println("Role " + selectedRole + " added to " + employee.getName());
        } catch (Exception e) {
            System.out.println("Failed to add role: " + e.getMessage());
        }
    }

    private static void addEmployeeMenu() {
        System.out.println("New employee adding");
        System.out.println("Enter name or 0 to go back:");
        int input = scanner.nextInt();
        if (input == 0)
            start();
        String name = String.valueOf(input);
        System.out.println("Enter ID:");
        int id = scanner.nextInt();
        System.out.println("Enter bank account:");
        int bankAccount = scanner.nextInt();
        System.out.println("Enter salary:");
        double salary = scanner.nextDouble();
        System.out.println("Enter terms:");
        System.out.println("Enter job status");
        System.out.println("1 for full time, 2 for half time");
        int choice = scanner.nextInt();
        JobStatus jobStatus=null;
        switch (choice) {
            case 1:
                 jobStatus = JobStatus.fullTime;
                break;
            case 2:
                 jobStatus = JobStatus.halfTime;
                break;
            default:
                System.out.println("Invalid choice.");

        }

        System.out.println("Enter salary type");
        System.out.println("1 for hourly, 2 for  global");
        choice = scanner.nextInt();
        SalaryType salaryType=null;
        switch (choice) {
            case 1:
                 salaryType = SalaryType.hourly;
                break;
            case 2:
                salaryType = SalaryType.global;
                break;
            default:
                System.out.println("Invalid choice.");

        }
        System.out.println("Enter rest days");
        int restDays = scanner.nextInt();
        EmployeeTerms terms = new EmployeeTerms(jobStatus ,salaryType,  restDays);
        System.out.println("Enter start date:");
        String startDateString = scanner.nextLine();
        LocalDate date = Parser.stringToDate(startDateString);
        employeeManager.add(name, id, bankAccount, salary, terms, date);
        Constraint constraint = new Constraint();
        constraintManager.getEmployeeConstraints().put(id, constraint);
        System.out.println("Employee added.");
        start();
    }
}
