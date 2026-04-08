package dev.Workers.presentation;

import dev.Workers.domain.*;

import java.time.LocalDate;

import static dev.Workers.presentation.Main.displayMenu;
import static dev.Workers.presentation.Main.scanner;

public class ManageEmployeesMenu {
    static EmployeeManager employeeManager = EmployeeManager.getInstance();
    static ConstraintManager constraintManager = ConstraintManager.getInstance();
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
                displayMenu();
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
        System.out.println("1. Update Constraints");
        System.out.println("2. Employee Details");
        System.out.println("3. Promote/Demote");
        System.out.println("4. Remove");
        System.out.println("5. Back");

        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                updateConstraints();
            case 2:
                details();
            case 3:
                promoteDemote();
            case 4:
                remove();
            case 5:
                accessEmployee();
            default:
                System.out.println("Invalid choice.");
        }
    }

    private static void updateConstraints() {
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
    }

    private static void details() {
        System.out.print("Employee Details");
        employee.toString();
        System.out.print("Choose 1-5 to update detail or 0 to go back:");
        int choice = scanner.nextInt();

        switch (choice) {
            case 0:
                manageEmployee();
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
                System.out.println("Enter new terms:");
                String newTerms = scanner.nextLine();
                employee.setTerms(newTerms);
                System.out.println("Terms updated.");
                details();
            default:
                System.out.println("Invalid choice.");
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
            case 2:
                accessEmployee();
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
        String terms = scanner.nextLine();
        System.out.println("Enter start date:");
        String startDateString = scanner.nextLine();
        LocalDate date = Parser.stringToDate(startDateString);
        
        employeeManager.add(name, id, bankAccount, salary, terms, date);
        System.out.println("Employee added.");
        start();
    }
}
