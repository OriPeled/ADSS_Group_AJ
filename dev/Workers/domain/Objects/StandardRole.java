package dev.Workers.domain.Objects;

import dev.Workers.domain.EmployeeHandler;

import java.util.Objects;

public class StandardRole implements Role {
    private final String name;

    public StandardRole(String name) { this.name = name; }

    @Override
    public String getName() { return name; }

    @Override
    public Requirement createDefaultRequirement() {
        return new Requirement(this, 0); // Or pass default amount in constructor
    }

    @Override
    public boolean isQualified(int id) {
        Employee emp = EmployeeHandler.getInstance().getEmployee(id);
        return emp.hasRole(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StandardRole that = (StandardRole) o;
        // Two roles are equal if their names are the same
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() { return name; }
}
