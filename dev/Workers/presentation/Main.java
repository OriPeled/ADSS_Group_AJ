package dev.Workers.presentation;
import java.util.Scanner;

public class Main {
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            displayMenu();
        }
    }

    public static void displayMenu() {
        System.out.println("Choose Mode:");
        System.out.println("1. User Mode");
        System.out.println("2. HR Manager Mode");
        System.out.println("3. Exit");

        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                UserMode.login();
                break;
            case 2:
                AdminMode.login();
                break;
            case 3:
                System.out.println("Have a good day.");
                //scanner.close();
                //break;
                System.exit(0);
            default:
                System.out.println("Invalid input.");
        }
    }
}