package dev;

import dev.Workers.database.DatabaseInitializer;
import dev.Workers.database.SeedData;
import dev.Workers.presentation.adminMode;
import dev.Workers.presentation.UserMode;
import java.util.Scanner;

public class Main {
    public static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        DatabaseInitializer.initializeDatabase();
        displayMenu();
    }

    public static void displayMenu() {
        while (true) {
            System.out.println("Choose Mode:");
            System.out.println("1. User Mode");
            System.out.println("2. HR Manager Mode");
            System.out.println("3. Load demo data to database");
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
                    SeedData.loadDemoData();
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