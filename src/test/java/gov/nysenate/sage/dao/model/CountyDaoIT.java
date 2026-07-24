package gov.nysenate.sage.dao.model;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.dao.model.county.CountyDao;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class CountyDaoIT extends BaseTests {
    @Autowired
    private CountyDao sqlCountyDao;

    @Test
    public void getCountiesTest() {
        assertEquals(62, sqlCountyDao.getCounties().size());
    }
}
