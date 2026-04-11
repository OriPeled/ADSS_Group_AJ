package dev.Workers.domain.Objects;

public class Role {
    // name of the role
    private String Rolename;
    /**
     * Constructor for creating a new role.
     *
     * @param Rolename the name of the role
     */
    public Role(String Rolename) {
        this.Rolename = Rolename;
    }
    /**
     * @return the role name
     */
    public String getRolename() {
        return Rolename;
    }

    /**
     * @return string representation of the role (its name)
     */
    @Override
    public String toString() {
        return Rolename;
    }
}
