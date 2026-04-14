package dev.Workers.domain;

public class Access {
    private String password;

    public Access(String password) {
        this.password = password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isWrongPassword(String password) {
        return !this.password.equals(password);
    }
}
