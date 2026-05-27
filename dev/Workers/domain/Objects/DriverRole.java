package dev.Workers.domain.Objects;

import dev.Workers.domain.EmployeeManager;
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

    @Override
    public String getName() { return "Driver (" + requiredLicense + ")"; }

    @Override
    public Requirement createDefaultRequirement() {
        return new Requirement(this, 0);
    }

    @Override
    public boolean isQualified(int empId) {
        Employee emp = EmployeeManager.getInstance().getById(empId);
        LicenseType empLicenseType = emp.getLicenseType(); // ?
        if (empLicenseType == LicenseType.NONE) return false;

        return emp.hasRole(this) && empLicenseType.compareTo(this.requiredLicense) >= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DriverRole that = (DriverRole) o;
        // Two roles are equal if their names are the same
        return Objects.equals(requiredLicense, that.requiredLicense);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requiredLicense);
    }
}
