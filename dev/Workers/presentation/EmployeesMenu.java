package dev.Workers.presentation;

import dev.Workers.domain.*;

import java.time.LocalDate;
import java.util.Date;

import static dev.Workers.presentation.Main.displayMenu;
import static dev.Workers.presentation.Main.scanner;

public class EmployeesMenu {
    static EmployeeManager employeeManager = EmployeeManager.getInstance();
    static ConstraintManager constraintManager = ConstraintManager.getInstance();
    static int id;
    static Employee employee;

    public static void employeesMenu() {
        System.out.println("Employees");
        System.out.println("1. Manage existing Employee");
        System.out.println("2. Add Employee");
        System.out.println("3. Back");

        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
               manageEmployeeMenu();
            case 2:
                addEmployeeMenu();
            case 3:
                displayMenu();
            default:
                System.out.println("Invalid choice.");
        }
    }

    private static void manageEmployeeMenu() {
        System.out.println("Manage Employee");
        System.out.println("Enter employee ID or enter 0 to go back:");
        id = scanner.nextInt();
        if (id == 0)
            employeesMenu();

        employee = employeeManager.getById(id);

        System.out.println("1. Update Constraints");
        System.out.println("2. Employee Details");
        System.out.println("3. Remove");
        System.out.println("4. Back");

        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                updateConstraints();
            case 2:
                details();
            case 3:
                remove();
            case 4:
                employeesMenu();
            default:
                System.out.println("Invalid choice.");
        }
    }

    private static void updateConstraints() {
        System.out.print("Update Constraints");
        System.out.print("Enter date (dd/mm/yyyy):");
        String dateString = scanner.nextLine();
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
                manageEmployeeMenu();
        }
    }

    private static void details() {
        System.out.print("Employee Details");
        employee.toString();
        System.out.print("Choose 1-4 to update detail or 5 for going back:");
        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                System.out.print("Enter new name:");
                String newName = scanner.nextLine();
                employee.setName(newName);
                System.out.println("Name updated.");
            case 2:
                System.out.println("Enter new bank account:");
                int newBankAccount = scanner.nextInt();
                employee.setBankAccount(newBankAccount);
                System.out.println("Bank account updated.");
            case 3:
                System.out.println("Enter new salary:");
                double newSalary = scanner.nextDouble();
                employee.setSalary(newSalary);
                System.out.println("Salary updated.");
            case 4:
                System.out.println("Enter new terms:");
                String newTerms = scanner.nextLine();
                employee.setTerms(newTerms);
                System.out.println("Terms updated.");
            case 5:
                manageEmployeeMenu();
            default:
                System.out.println("Invalid choice.");
        }
    }

    private static void remove() {
        System.out.print("Remove Employee");
        System.out.println("Enter ID:");
        id = scanner.nextInt();

        employee = employeeManager.getById(id);
        String name = employee.getName();
        System.out.println("Are you sure you want to remove " + name + " (" + id + ")? If yes - enter 1, else 0.");

        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                employeeManager.remove(id);
                System.out.println("Employee removed.");
                manageEmployeeMenu();
            case 2:
                manageEmployeeMenu();
        }
    }

    private static void addEmployeeMenu() {
        System.out.println("New employee adding");
        System.out.println("Enter name:");
        String name = scanner.nextLine();
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
        employeesMenu();
    }
}
