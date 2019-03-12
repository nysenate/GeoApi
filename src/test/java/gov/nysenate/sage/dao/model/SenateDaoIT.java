package gov.nysenate.sage.dao.model;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.dao.model.senate.SqlSenateDao;
import gov.nysenate.services.model.Senator;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class SenateDaoIT extends BaseTests {

    @Autowired
    SqlSenateDao sqlSenateDao;
    private static int NUMBER_OF_SENATORS = 63;

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void getSenatorsTest()
    {
        Collection<Senator> senators = this.sqlSenateDao.getSenators();
        assertNotNull(senators);
        assertEquals(NUMBER_OF_SENATORS, senators.size());

        for (Senator senator : senators){
            assertNotNull(senator.getDistrict());
            assertNotNull(senator.getName());
        }
    }

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void getSenatorByDistrictNumberTest()
    {
        Senator senator = this.sqlSenateDao.getSenatorByDistrict(44);
        assertNotNull(senator);
    }
}
