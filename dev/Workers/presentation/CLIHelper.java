package dev.Workers.presentation;

import dev.Workers.domain.Enums.LicenseType;

import static dev.Main.scanner;

public class CLIHelper {
    public static int readIntSafe() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again:");
            }
        }
    }

    public static double readDoubleSafe() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again:");
            }
        }
    }

    public static LicenseType getLicenseTypeFromNumber(int licenseTypeNumber) {
        while (true) {
            if (licenseTypeNumber < 0 || licenseTypeNumber >= LicenseType.values().length) {
                throw new IllegalArgumentException("Invalid license type choice.");
            }
            return LicenseType.values()[licenseTypeNumber];
        }
    }
}
