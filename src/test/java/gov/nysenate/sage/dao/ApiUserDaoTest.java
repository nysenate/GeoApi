package gov.nysenate.sage.dao;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.auth.ApiUser;
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
    private Config config;
    private String defaultKey;
    private String defaultName;

    @Before
    public void setUp()
    {
        config = ApplicationFactory.getConfig();
        this.apiUserDao = new ApiUserDao();
        this.defaultKey = config.getValue("user.default");
        this.defaultName = config.getValue("user.default.name");
    }

    @Test
    public void getApiUserById_ReturnsApiUser()
    {
        ApiUser apiUser = apiUserDao.getApiUserById(1);
        assertNotNull(apiUser);
        assertEquals((int)1, (int)apiUser.getId());
        assertEquals(this.defaultKey, apiUser.getApiKey());
        assertEquals(this.defaultName, apiUser.getName());
    }

    @Test
    public void getApiUserByKey_ReturnsApiUser()
    {
        ApiUser apiUser = apiUserDao.getApiUserByKey(this.defaultKey);
        assertNotNull(apiUser);
        assertEquals((int)1, (int)apiUser.getId());
        assertEquals(this.defaultKey, apiUser.getApiKey());
        assertEquals(this.defaultName, apiUser.getName());
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
        assertEquals(1, inserts);

        ApiUser retUser = apiUserDao.getApiUserByKey(uniqueKey);
        assertNotNull(retUser);
        assertEquals("tester", retUser.getName());
        assertEquals(uniqueKey, retUser.getApiKey());

        int deletes = apiUserDao.removeApiUser(retUser);
        assertEquals(1, deletes);

        ApiUser deletedUser = apiUserDao.getApiUserByKey(uniqueKey);
        assertNull(deletedUser);
    }

}
