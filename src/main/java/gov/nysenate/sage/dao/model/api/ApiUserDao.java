package gov.nysenate.sage.dao.model.api;

import gov.nysenate.sage.model.api.ApiUser;

import java.util.List;

public interface ApiUserDao {
    /**
     * Retrieves an ApiUser from the database by key.
     * @param id         The api user id.
     * @return ApiUser   The matched ApiUser or null if not found.
     */
    public ApiUser getApiUserById(int id);

    /**
     * Retrieves an ApiUser from the database by name.
     * @param name         The api user name.
     * @return ApiUser   The matched ApiUser or null if not found.
     */
    public ApiUser getApiUserByName(String name);

    /**
     * Retrieves an ApiUser from the database by key.
     * @param key        The api key.
     * @return ApiUser   The matched ApiUser or null if not found.
     */
    public ApiUser getApiUserByKey(String key);

    /**
     * Retrieves all ApiUsers.
     * @return      List of ApiUser
     */
    public List<ApiUser> getApiUsers();

    /**
     * Adds an API User to the database.
     * @param apiUser   The ApiUser to add.
     * @return int      1 if user was inserted, 0 otherwise.
     */
    public int addApiUser(ApiUser apiUser);

    /**
     * Removes an API User from the database.
     * @param apiUser   The ApiUser to add.
     * @return int      1 if user was removed, 0 otherwise.
     */
    public int removeApiUser(ApiUser apiUser);
}
