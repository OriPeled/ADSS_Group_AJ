package dev.Workers.database.dao;

import java.util.List;
import java.util.Optional;

/**
 * Generic Data Access Object interface.
 * <p>
 * Follows the DAO pattern presented in the course (Software Architecture /
 * Persistence lectures): the DAO is a data-mapping/access layer that hides the
 * SQL queries from the business logic. Each concrete DAO maps a single domain
 * (business) object type {@code T} to its underlying relational representation.
 * <p>
 * The in-memory Identity Map (the domain Handlers, e.g. EmployeeHandler) is the
 * first lookup and the source of truth; the DAO is the persistence layer behind
 * it. Mutations performed through the Handlers are written through to the DB via
 * {@code save}/{@code update}/{@code delete}, and {@code getAll} is used to
 * reload the Identity Map on startup.
 *
 * @param <T> the domain object type handled by this DAO
 */
public interface Dao<T> {
    Optional<T> get(long id);

    List<T> getAll();

    void save(T t);

    void update(T t);

    void delete(T t);
}