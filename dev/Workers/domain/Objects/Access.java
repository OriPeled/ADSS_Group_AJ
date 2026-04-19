package dev.Workers.domain.Objects;

import java.util.Objects;

public class Access {


    private String password;

    public Access(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isWrongPassword(String password) {
        return !this.password.equals(password);
    }

    public boolean isPasswordEmpty(String password) {
        return password == null || password.isEmpty();
    }
}
