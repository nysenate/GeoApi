package gov.nysenate.sage.dao.model.admin;

import gov.nysenate.sage.model.admin.AdminUser;

public interface AdminUserDao {

    /**
     * Check if the admin user credentials are valid.
     * @param username  Admin username
     * @param password  Admin password
     * @return true if valid credentials, false otherwise.
     */
    boolean checkAdminUser(String username, String password);

    /**
     * Used for Shiro Authentication
     * @return AdminUser object corresponding with the admin of that username
     */
    AdminUser getAdminUser(String username);

    /**
     * Inserts a new admin into the db
     * NOTE: the password should be hashed with Bcrypt before passing it into this method
     */
    void insertAdmin(String username, String password);
}
