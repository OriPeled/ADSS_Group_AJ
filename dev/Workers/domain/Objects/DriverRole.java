package dev.Workers.domain.Objects;

import dev.Workers.domain.EmployeeHandler;
import dev.Workers.domain.Enums.LicenseType;

import java.util.Objects;

public class DriverRole implements Role {
    private final LicenseType requiredLicense;

    public DriverRole(LicenseType requiredLicense) {
        this.requiredLicense = requiredLicense;
    }

    public DriverRole() {
        this.requiredLicense = LicenseType.NONE;
    }

    public LicenseType getRequiredLicense() {
        return requiredLicense;
    }
    @Override
    public String getName() { return "Driver (" + requiredLicense + ")"; }

    @Override
    public Requirement createDefaultRequirement() {
        return new Requirement(this, 0);
    }

    @Override
    public boolean isQualified(int id) {
        Employee emp = EmployeeHandler.getInstance().getEmployee(id);
        if (emp == null) return false;
        return emp.hasRole(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DriverRole that = (DriverRole) o;
        return this.requiredLicense == that.requiredLicense;
    }

    @Override
    public int hashCode() {
        return Objects.hash(requiredLicense);
    }

    public String toString() {
        { return "Driver (" + requiredLicense + ")"; }
    }
}
