package dev.Workers.domain;

/**
 * Generic interface for managing entities in the system.
 *
 * Defines basic CRUD-like operations:
 * - Add entity
 * - Remove entity
 * - Retrieve entity by ID
 *
 * @param <T> the type of object being managed
 */
public interface IManager<T> {

    /**
     * Adds a new entity to the system
     *
     * @param id unique identifier of the entity
     * @param t the entity object to add
     */
    void add(int id, T t);

    /**
     * Removes an entity from the system by ID
     *
     * @param id unique identifier of the entity
     */
    void remove(int id);

    /**
     * Retrieves an entity by its ID
     *
     * @param id unique identifier of the entity
     * @return the entity object, or null if not found
     */
    T getById(int id);
}