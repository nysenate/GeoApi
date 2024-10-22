package gov.nysenate.sage.dao.provider;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.dao.provider.tiger.SqlTigerDao;
import gov.nysenate.sage.model.geo.Line;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class TigerDaoIT extends BaseTests {
    @Autowired
    SqlTigerDao sqlTigerGeocoderDao;

    @Test
    @Transactional(value = DatabaseConfig.geocoderTxManager)
    public void getStreetLineGeometryTest() {
        List<Line> lines = sqlTigerGeocoderDao.getStreetLineGeometry("State St", List.of(12203, 12210));
        assertNotNull(lines);
    }
}
