package gov.nysenate.sage.dao.data;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.config.DatabaseConfig;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class RegeocacheDaoIT extends BaseTests {
    @Autowired
    private SqlRegeocacheDao sqlRegeocacheDao;

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void getAllZipsTest() {
        List<String> zips = sqlRegeocacheDao.getAllZips();
        assertTrue(zips.size() >= 1794);
    }
}
