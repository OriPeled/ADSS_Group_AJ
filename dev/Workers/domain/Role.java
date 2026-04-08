package dev.Workers.domain;

public class Role {
    private String Rolename;

    public Role(String Rolename) {
        this.Rolename = Rolename;
    }

    public String getRolename() {
        return Rolename;
    }


    @Override
    public String toString() {
        return "Role{" +
                "Rolename='" + Rolename + '\'' +
                '}';
    }
}
