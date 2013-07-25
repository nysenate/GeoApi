package gov.nysenate.sage.model.admin;

import java.io.Serializable;

public class AdminUser implements Serializable
{
    private int id;
    private String username;
    private String password;

    public AdminUser() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
