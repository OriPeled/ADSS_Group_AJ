package dev.Workers.presentation;

import dev.Workers.Service.ConstraintService;
import dev.Workers.Service.EmployeeService;
import dev.Workers.Service.RoleService;
import dev.Workers.domain.Enums.JobStatus;
import dev.Workers.domain.Enums.Role;
import dev.Workers.domain.Enums.SalaryType;
import dev.Workers.domain.Objects.Constraint;
import dev.Workers.domain.Objects.Employee;
import dev.Workers.domain.EmployeeTerms;

import java.time.LocalDate;

import static dev.Workers.presentation.Main.scanner;

public class ManageEmployeesMenu {
    static EmployeeService employeeService = EmployeeService.getInstance();
    static ConstraintService constraintService = ConstraintService.getInstance();
    static RoleService roleService = RoleService.getInstance();
    static int id;

    public static void start() {
        while (true) {
            System.out.println("Employees");
            System.out.println("1. Manage existing Employee");
            System.out.println("2. Add Employee");
            System.out.println("3. Back");

            int choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 1:
                    accessEmployee();
                    break;
                case 2:
                    addEmployeeMenu();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void accessEmployee() {
        System.out.println("Manage Employee");
        System.out.println("Enter employee ID or enter 0 to go back:");
        id = Integer.parseInt(scanner.nextLine());
        if (id == 0)
            return;

        try {
            if (!employeeService.exists(id)) {
                System.out.println("Employee not found.");
                return;
            }
            System.out.println("Employee chosen");
            manageEmployee();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void manageEmployee() {
        while (true) {
            System.out.println(employeeService.getEmployeeName(id) + " (" + id + ")");
            System.out.println("2. Employee Details");
            System.out.println("3. Promote/Demote");
            System.out.println("4. Remove");
            System.out.println("5. Add Role");
            System.out.println("6. Back");

            int choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 2:
                    details();
                    break;
                case 3:
                    promoteDemote();
                    break;
                case 4:
                    remove();
                    return;           // after remove, go back up
                case 5:
                    addRole();
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

/*    private static void updateConstraints() {
        System.out.print("Update Constraints");
        System.out.print("Enter date (dd/mm/yyyy) or 0 to go back:");
        int input = Integer.parseInt(scanner.nextLine());
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
        int choice = Integer.parseInt(scanner.nextLine());
        switch (choice) {
            case 1:
                updateConstraints();
            case 2:
                manageEmployee();
        }
    }*/

    private static void details() {
        while (true) {
            System.out.print(employeeService.getEmployeeDetails(id));
            System.out.print("Choose 1-4 to update detail or 0 to go back:");
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    System.out.print("Enter new name:");
                    String newName = scanner.nextLine();
                    try {
                        employeeService.updateName(id, newName);
                        System.out.println("Name updated.");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;
                case 2:
                    System.out.println("Enter new bank account:");
                    int newBankAccount = Integer.parseInt(scanner.nextLine());
                    try {
                        employeeService.updateBankAccount(id, newBankAccount);
                        System.out.println("Bank account updated.");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;
                case 3:
                    System.out.println("Enter new salary:");
                    double newSalary = Double.parseDouble(scanner.nextLine());
                    try {
                        employeeService.updateSalary(id, newSalary);
                        System.out.println("Salary updated.");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;
                case 4:
                    updateTerms();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    public static void updateTerms() {
        while (true) {
            System.out.println(employeeService.getEmployeeTermsDisplay(id));  // read-only display
            System.out.println("Choose 1-3 to update detail or 0 to go back:");
            int choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 1:
                    System.out.println("Enter 1 to change job status, 0 to cancel:");
                    int choice1 = Integer.parseInt(scanner.nextLine());
                    if (choice1 == 1) {
                        try {
                            employeeService.updateJobStatus(id);
                            System.out.println("Job Status updated.");
                        } catch (IllegalArgumentException e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                    }
                    break;
                case 2:
                    System.out.println("Enter 1 to change salary type, 0 to cancel:");
                    int choice2 = Integer.parseInt(scanner.nextLine());
                    if (choice2 == 1) {
                        try {
                            employeeService.updateSalaryType(id);
                            System.out.println("Salary Type updated.");
                        } catch (IllegalArgumentException e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                    }
                    break;
                case 3:
                    System.out.println("Enter number of rest days (1-7) or 0 to cancel:");
                    int newRestDays = Integer.parseInt(scanner.nextLine());
                    if (newRestDays == 0) break;
                    try {
                        employeeService.updateRestDays(id, newRestDays);
                        System.out.println("Rest days updated.");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void promoteDemote() {
        roleService.promoteDemote(id);
    }

    private static void remove() {
        System.out.println("Are you sure you want to remove " + employeeService.getEmployeeName(id) + " (" + id + ")?" + " If yes - enter 1, else 0.");

        int choice = Integer.parseInt(scanner.nextLine());
        if (choice == 1) {
            try {
                employeeService.remove(id);
                System.out.println("Employee removed.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
    /**
     * Adds a role to an employee after validating input.
     */
    private static void addRole() {
        Role[] roles = Role.values();
        System.out.println("Choose role to add:");
        int choice = Integer.parseInt(scanner.nextLine());
        for (int i = 0; i < roles.length; i++) {
            System.out.println((i + 1) + ". " + roles[i]);
        }
        if (choice < 1 || choice > roles.length) {
            System.out.println("Invalid choice.");
            return;
        }
        Role selectedRole = roles[choice - 1];
        try {
            roleService.addRoleToEmployee(id, selectedRole);
            System.out.println("Role " + selectedRole + " added to " + employeeService.getEmployeeName(id));
        } catch (Exception e) {
            System.out.println("Failed to add role: " + e.getMessage());
        }
    }

    private static void addEmployeeMenu() {
        System.out.println("New employee adding");
        System.out.println("Enter name or 0 to go back:");
        scanner.nextLine();                           // consume leftover newline
        String name = scanner.nextLine();
        if (name.equals("0")) return;
        System.out.println("Enter ID:");
        int id = Integer.parseInt(scanner.nextLine());
        System.out.println("Enter bank account:");
        int bankAccount = Integer.parseInt(scanner.nextLine());
        System.out.println("Enter salary:");
        double salary = scanner.nextDouble();
        System.out.println("Enter terms:");
        System.out.println("Enter job status");
        System.out.println("1 for full time, 2 for half time");
        int choice = Integer.parseInt(scanner.nextLine());
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
        choice = Integer.parseInt(scanner.nextLine());
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
        int restDays = Integer.parseInt(scanner.nextLine());
        EmployeeTerms terms = new EmployeeTerms(jobStatus ,salaryType,  restDays);
        System.out.println("Enter start date:");
        String startDateString = scanner.nextLine();
        LocalDate date = Parser.stringToDate(startDateString);
        employeeService.add(name, id, bankAccount, salary, terms, date);
        Constraint constraint = new Constraint();
        constraintService.getEmployeeConstraints().put(id, constraint);
        System.out.println("Employee added.");

        manageEmployee();
    }
}
