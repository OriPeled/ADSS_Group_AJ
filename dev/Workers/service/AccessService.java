package dev.Workers.service;

import dev.Workers.domain.Enums.UserResponse;
import dev.Workers.domain.AccessHandler;

/**
 * AccessService provides a high-level API for managing employee credentials.
 * It acts as a facade for the {@link AccessHandler} domain class, handling
 * business-level validations and authentication logic.
 * * This class is implemented as a Singleton.
 */
public class AccessService {
    private final AccessHandler accessHandler;

    private static AccessService instance;

    private AccessService() {
        this.accessHandler = AccessHandler.getInstance();
    }

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
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
         accessHandler.register(id, password);
    }

    public UserResponse login(int id, String password) {
        return accessHandler.login(id, password);
    }

    /**
     * Removes an employee's access credentials from the system.
     * * @param id The unique identifier of the employee to remove.
     * @throws IllegalArgumentException If the user ID does not exist in the system.
     */
    public void removeUser(int id) {
        accessHandler.remove(id);
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
        accessHandler.updatePassword(id, newPassword);
    }
}