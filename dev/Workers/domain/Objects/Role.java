package dev.Workers.domain.Objects;

public interface Role {
    String getName();

    Requirement createDefaultRequirement();

    boolean isQualified(int id);

    public String toString();
}
