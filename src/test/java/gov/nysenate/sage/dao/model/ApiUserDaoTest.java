package gov.nysenate.sage.dao.model;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.api.ApiUser;
import static org.junit.Assert.*;

import gov.nysenate.sage.util.Config;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 * Test to verify ApiUserDao performs basic operations correctly.
 */
public class ApiUserDaoTest extends TestBase
{
    private ApiUserDao apiUserDao;
    private String defaultKey;

    @Before
    public void setUp()
    {
        Config config = ApplicationFactory.getConfig();
        this.apiUserDao = new ApiUserDao();
        this.defaultKey = config.getValue("user.default.key");
    }

    @Test
    public void getApiUserById_ReturnsApiUser()
    {
        int userId = 1;
        ApiUser apiUser = apiUserDao.getApiUserById(userId);
        assertNotNull(apiUser);
        assertEquals(userId, apiUser.getId());
        assertEquals(this.defaultKey, apiUser.getApiKey());
    }

    @Test
    public void getApiUserByKey_ReturnsApiUser()
    {
        ApiUser apiUser = apiUserDao.getApiUserByKey(this.defaultKey);
        assertNotNull(apiUser);
        assertEquals(1, apiUser.getId());
        assertEquals(this.defaultKey, apiUser.getApiKey());
    }

    @Test
    public void getApiUserByInvalidKey_ReturnsNull()
    {
        ApiUser apiUser = apiUserDao.getApiUserByKey("INVALID");
        assertNull(apiUser);
    }

    @Test
    public void addGetRemoveApiUser_WorksProperly()
    {
        String uniqueKey = "new_testing_key" + new Date().getTime();

        ApiUser user = new ApiUser();
        user.setApiKey(uniqueKey);
        user.setName("tester");
        user.setDescription("Added from test");

        int inserts = apiUserDao.addApiUser(user);

        ApiUser retUser = apiUserDao.getApiUserByKey(uniqueKey);
        assertNotNull(retUser);
        assertEquals("tester", retUser.getName());
        assertEquals(uniqueKey, retUser.getApiKey());

        int deletes = apiUserDao.removeApiUser(retUser);
        assertEquals(inserts, deletes);

        ApiUser deletedUser = apiUserDao.getApiUserByKey(uniqueKey);
        assertNull(deletedUser);
    }

}