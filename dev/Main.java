package dev;

import dev.Workers.database.DatabaseManager;
import dev.Workers.setup.DataInitializer;
import dev.Workers.presentation.adminMode;
import dev.Workers.presentation.UserMode;
import java.util.Scanner;

import dev.Workers.database.DatabaseInitializer;
import dev.Workers.database.dao.*;
import dev.Workers.domain.*;
import dev.Workers.domain.Objects.*;
import java.util.Map;

public class Main {
    public static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        DatabaseInitializer.initializeDatabase();
        loadAllFromDatabase();
        displayMenu();
    }

    private static void loadAllFromDatabase() {
        EmployeeHandler employeeHandler = EmployeeHandler.getInstance();
        PreferenceHandler preferenceHandler = PreferenceHandler.getInstance();
        AccessHandler accessHandler = AccessHandler.getInstance();
        ShiftHandler shiftHandler = ShiftHandler.getInstance();

        EmployeeDaoSQL employeeDao = EmployeeDaoSQL.getInstance();
        PreferenceDaoSQL preferenceDao = PreferenceDaoSQL.getInstance();
        AccessDaoSQL accessDao = AccessDaoSQL.getInstance();
        WeekScheduleDaoSQL weekScheduleDao = WeekScheduleDaoSQL.getInstance();

        // 1. Employees (must come first — everything references them)
        for (Employee e : employeeDao.getAll()) {
            employeeHandler.getEmployees().put(e.getId(), e);
        }

        // 2. Preferences
        for (Map.Entry<Integer, Preference> entry : preferenceDao.getAll().entrySet()) {
            preferenceHandler.getAllPreferences().put(entry.getKey(), entry.getValue());
        }

        // 3. Access credentials
        for (Map.Entry<Integer, Access> entry : accessDao.getAll().entrySet()) {
            accessHandler.restore(entry.getKey(), entry.getValue());
        }

        // 4. Shifts + requirements + assignments + extra hours
        shiftHandler.loadAll();

        // 5. Week schedules (publication flags)
        for (WeekSchedule week : weekScheduleDao.getAll()) {
            ShiftHandler.getOrCreateWeek(week.getStartOfWeek()).setPublished(week.isPublished());
        }
    }

    public static void displayMenu() {
        while (true) {
            System.out.println("Choose Mode:");
            System.out.println("1. User Mode");
            System.out.println("2. HR Manager Mode");
            System.out.println("3. Load demo data to database");
            System.out.println("4. Erase database");
            System.out.println("0. Exit");

            String input = scanner.nextLine();
            int choice;

            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                continue;
            }

            switch (choice) {
                case 1:
                    UserMode.login();
                    break;
                case 2:
                    adminMode.login();
                    break;
                case 3:
                    if (!EmployeeHandler.getInstance().getEmployees().isEmpty()) {
                        System.out.println("Demo data is already loaded in the database. ");
                    } else {
                        DataInitializer.initSystem();
                    }
                    //DataInitializer.initSystem();
                    break;
                case 4:
                    System.out.println("Erasing database...");
                    DatabaseManager.eraseDatabase();
                    System.out.println("Database erased successfully.");
                    System.out.println("Exiting system to clear in-memory data. Please restart the app.");
                    scanner.close();
                    System.exit(0);
                    break;
                case 0:
                    System.out.println("Have a good day.");
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("Invalid input.");
            }
        }
    }
}