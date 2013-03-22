package gov.nysenate.sage.model.job;

import java.sql.Timestamp;

public class JobUser
{
    protected int id;
    protected String email;
    protected String password;
    protected String firstname;
    protected String lastname;
    protected Timestamp joined;
    protected boolean active;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Timestamp getJoined() {
        return joined;
    }

    public void setJoined(Timestamp timestamp) {
        this.joined= timestamp;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
