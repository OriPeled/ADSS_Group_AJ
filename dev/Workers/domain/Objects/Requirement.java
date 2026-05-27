package dev.Workers.domain.Objects;

public class Requirement {
    private final Role role;
    private int amount;

    public Requirement(Role role, int amount) {
        this.role = role;
        this.amount = amount;
    }

    public Role getRole() {
        return role;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amount = amount;
    }
}
