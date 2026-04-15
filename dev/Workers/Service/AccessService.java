package dev.Workers.Service;

import dev.Workers.domain.Access;
import java.util.HashMap;
import java.util.Map;

/**
 * Service class responsible for managing user credentials and access control.
 * This class follows the Singleton pattern to ensure a single point of access
 * to the credentials database.
 */
public class AccessService {

    /**
     * Map storing the relationship between Employee ID and their Access credentials.
     * Key: Integer (Employee ID)
     * Value: Access (Password/Credential object)
     */
    private Map<Integer, Access> accessMap;

    /** The single instance of the service */
    private static AccessService instance;

    /**
     * Private constructor to prevent external instantiation.
     */
    private AccessService() {
        this.accessMap = new HashMap<>();
    }

    /**
     * Retrieves the singleton instance of AccessService.
     * * @return The active instance of AccessService.
     */
    public static AccessService getInstance() {
        if (instance == null) {
            instance = new AccessService();
        }
        return instance;
    }

    /**
     * Registers a new user in the access system.
     * * @param id The unique identifier of the employee.
     * @param password The password to be assigned to the user.
     * @throws IllegalArgumentException if the password is null/empty or if the user already exists.
     */
    public void Register(int id, String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        if (accessMap.containsKey(id)) {
            throw new IllegalArgumentException("User with ID " + id + " is already registered.");
        }

        accessMap.put(id, new Access(password));
    }

    /**
     * Removes a user's access credentials from the system.
     * * @param id The unique identifier of the employee to remove.
     * @throws IllegalArgumentException if the user ID is not found in the system.
     */
    public void Remove(int id) {
        if (accessMap.containsKey(id)) {
            accessMap.remove(id);
        } else {
            throw new IllegalArgumentException("User ID not found.");
        }
    }

    /**
     * Updates the password for an existing registered user.
     * * @param id The unique identifier of the employee.
     * @param newPassword The new password to be set.
     * @throws IllegalArgumentException if the user is not registered in the system.
     */
    public void updatePassword(int id, String newPassword) {
        Access access = accessMap.get(id);
        if (access != null) {
            access.setPassword(newPassword);
        } else {
            throw new IllegalArgumentException("Update failed: User not found.");
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