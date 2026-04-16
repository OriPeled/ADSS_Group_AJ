package dev.Workers.domain.Objects;

public class Access {
    public String getPassword() {
        return password;
    }

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
    public boolean isPasswordEmpty(String password) {
        return password.equals("") || password ==null;
    }
}
