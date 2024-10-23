package gov.nysenate.sage.dao.model;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.dao.provider.cityzip.SqlCityZipDBDao;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class CityZipDaoIT extends BaseTests {

    @Autowired
    SqlCityZipDBDao sqlCityZipDBDao;

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void testGetZipsByCity() {
        assertNotNull( sqlCityZipDBDao.getZipsByCity("troy"));
    }
}
