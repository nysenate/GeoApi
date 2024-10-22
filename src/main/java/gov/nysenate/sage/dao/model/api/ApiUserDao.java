package gov.nysenate.sage.dao.model.api;

import gov.nysenate.sage.model.api.ApiUser;

import java.util.List;

public interface ApiUserDao {
    /**
     * Retrieves an ApiUser from the database by key.
     * @param id         The api user id.
     * @return ApiUser   The matched ApiUser or null if not found.
     */
    ApiUser getApiUserById(int id);

    /**
     * Retrieves an ApiUser from the database by key.
     * @param key        The api key.
     * @return ApiUser   The matched ApiUser or null if not found.
     */
    ApiUser getApiUserByKey(String key);

    /**
     * Retrieves all ApiUsers.
     * @return      List of ApiUser
     */
    List<ApiUser> getApiUsers();

    /**
     * Adds an API User to the database.
     * @param apiUser   The ApiUser to add.
     * @return int      1 if user was inserted, 0 otherwise.
     */
    int addApiUser(ApiUser apiUser);

    /**
     * Removes an API User from the database.
     * @param apiUser The ApiUser to add.
     */
    void removeApiUser(ApiUser apiUser);
}
