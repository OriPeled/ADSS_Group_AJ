package dev.Workers.domain;

import dev.Workers.domain.Enums.Status;

import java.util.HashMap;
import java.util.Map;

/**
 * Service class responsible for managing user credentials and access control.
 * This class follows the Singleton pattern to ensure a single point of access
 * to the credentials database.
 */
public class AccessManager {

    /**
     * Map storing the relationship between Employee ID and their Access credentials.
     * Key: Integer (Employee ID)
     * Value: Access (Password/Credential object)
     */
    private Map<Integer, Access> accessMap;

    /** The single instance of the service */
    private static AccessManager instance;

    /**
     * Private constructor to prevent external instantiation.
     */
    private AccessManager() {
        this.accessMap = new HashMap<>();
    }

    /**
     * Retrieves the singleton instance of AccessService.
     * * @return The active instance of AccessService.
     */
    public static AccessManager getInstance() {
        if (instance == null) {
            instance = new AccessManager();
        }
        return instance;
    }

    /**
     * Registers a new user in the access system.
     * * @param id The unique identifier of the employee.
     * @param password The password to be assigned to the user.
     * @throws IllegalArgumentException if the password is null/empty or if the user already exists.
     */
    public Status Register(int id, String password) {
        if (password == null || password.trim().isEmpty()) {
            return Status.failure;
        }

        if (accessMap.containsKey(id)) {
            return Status.failure;
        }
        accessMap.put(id, new Access(password));
        return Status.success;
    }

    /**
     * Removes a user's access credentials from the system.
     * * @param id The unique identifier of the employee to remove.
     * @throws IllegalArgumentException if the user ID is not found in the system.
     */
    public Status Remove(int id) {
        if (accessMap.containsKey(id)) {
            accessMap.remove(id);
            return Status.success;
        } else {
            return Status.failure;
        }
    }

    /**
     * Updates the password for an existing registered user.
     * * @param id The unique identifier of the employee.
     * @param newPassword The new password to be set.
     * @throws IllegalArgumentException if the user is not registered in the system.
     */
    public Status updatePassword(int id, String newPassword) {
        Access access = accessMap.get(id);
        if (access != null) {
            access.setPassword(newPassword);
            return Status.success;
        } else {
            return Status.failure;
        }
    }

    /**
     * Checks if a specific employee ID is registered in the access system.
     * * @param id The employee ID to check.
     * @return true if the user is registered, false otherwise.
     */
    public boolean isRegisteredUser(int id) {
        return accessMap.containsKey(id);
    }
    public Access getAccess(int id) {
        return accessMap.get(id);
    }
}