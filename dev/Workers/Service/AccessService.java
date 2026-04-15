package dev.Workers.Service;

import dev.Workers.domain.Access;
import dev.Workers.domain.AccessManager;

/**
 * AccessService provides a high-level API for managing employee credentials.
 * It acts as a facade for the {@link AccessManager} domain class, handling
 * business-level validations and authentication logic.
 * * This class is implemented as a Singleton.
 */
public class AccessService {

    /** Reference to the domain-level access manager */
    private final AccessManager accessManager;

    /** The single instance of AccessService */
    private static AccessService instance;

    /**
     * Private constructor to enforce Singleton pattern and initialize the domain reference.
     */
    private AccessService() {
        this.accessManager = AccessManager.getInstance();
    }

    /**
     * Returns the singleton instance of the AccessService.
     * * @return The active AccessService instance.
     */
    public static AccessService getInstance() {
        if (instance == null) {
            instance = new AccessService();
        }
        return instance;
    }

    /**
     * Registers a new employee in the access system with a specified password.
     * Includes basic validation for password length.
     * * @param id       The unique identifier of the employee.
     * @param password The password to be assigned (must be at least 4 characters).
     * @throws IllegalArgumentException If the password is invalid or the user is already registered.
     */
    public void Register(int id, String password) {
        if (password == null || password.length() < 4) {
           return;
        }
        accessManager.Register(id, password);
    }

    /**
     * Removes an employee's access credentials from the system.
     * * @param id The unique identifier of the employee to remove.
     * @throws IllegalArgumentException If the user ID does not exist in the system.
     */
    public void removeUser(int id) {
        accessManager.Remove(id);
    }

    /**
     * Updates the password for an existing registered user.
     * * @param id          The unique identifier of the employee.
     * @param newPassword The new password to be set.
     * @throws IllegalArgumentException If the user is not found or password is invalid.
     */
    public void updatePassword(int id, String newPassword) {
        if (newPassword == null || newPassword.length() < 4) {
            throw new IllegalArgumentException("New password must be at least 4 characters.");
        }
        accessManager.updatePassword(id, newPassword);
    }

    /**
     * Checks if a specific employee ID has registered credentials in the system.
     * * @param id The employee ID to check.
     * @return {@code true} if the user is registered; {@code false} otherwise.
     */
    public boolean isRegisteredUser(int id) {
        return accessManager.isRegisteredUser(id);
    }

    /**
     * Retrieves the Access object containing credentials for a specific employee.
     * * @param id The unique identifier of the employee.
     * @return The {@link Access} object, or {@code null} if the user is not found.
     */
    public Access getAccess(int id) {
        return accessManager.getAccess(id);
    }

    /**
     * Authenticates a user by comparing a provided password with the stored one.
     * * @param id               The employee ID attempting to log in.
     * @param providedPassword The password entered by the user.
     * @return {@code true} if credentials match; {@code false} if user not found or password incorrect.
     */
    public boolean authenticate(int id, String providedPassword) {
        Access access = accessManager.getAccess(id);
        if (access == null || providedPassword == null) {
            return false;
        }
        return access.getPassword().equals(providedPassword);
    }
}