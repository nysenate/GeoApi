package gov.nysenate.sage.dao.data;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.NYSGeoAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.util.StreetAddressParser;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class RegeocacheDaoTest extends BaseTests {

    @Autowired
    SqlRegeocacheDao sqlRegeocacheDao;

    private static Logger logger = LoggerFactory.getLogger(RegeocacheDaoTest.class);

    /**
     * THIS TEST IS VALID, BUT SHOULD NOT BE RUN ON BUILDING.
     * A NEW INSTALL OF SAGE WILL NOT HAVE THE NYS GEO DB TABLE
     */
//    @Test
//    @Transactional(value = DatabaseConfig.geocoderTxManager)
//    public void retrieveTotalNYSAddressesTest() {
//        int total = sqlRegeocacheDao.getNYSTotalAddresses();
//        assertNotNull(total);
//        logger.info("The total number of addresses in the NYSGEO DB is " + total);
//
//    }

    /**
     * THIS TEST IS VALID, BUT SHOULD NOT BE RUN ON BUILDING.
     * A NEW INSTALL OF SAGE WILL NOT HAVE THE NYS GEO DB TABLE
     */
//    @Test
//    @Transactional(value = DatabaseConfig.geocoderTxManager)
//    public void retrieveBatchTest() {
//        List<NYSGeoAddress> nysGeoAddresses = sqlRegeocacheDao.getBatchOfNYSGeoAddresses(2000, 18000);
//        assertNotNull(nysGeoAddresses);
//    }

    /**
     * THIS TEST IS VALID, BUT SHOULD NOT BE RUN ON BUILDING.
     * A NEW INSTALL OF SAGE WILL NOT HAVE THE NYS GEO DB TABLE
     */
//    @Test
//    @Transactional(value = DatabaseConfig.geocoderTxManager)
//    public void retrieveBatchesTest() {
//        int offset = 0;
//        int limit = 2000;
//        int total = sqlRegeocacheDao.getNYSTotalAddresses();
//        while (offset < total) {
//            logger.info("Starting offset " + offset);
//            List<NYSGeoAddress> nysGeoAddresses =
//                    sqlRegeocacheDao.getBatchOfNYSGeoAddresses(limit, offset);
//            assertNotNull(nysGeoAddresses);
//            offset = offset + limit;
//        }
//    }

    /**
     * THIS TEST IS VALID, BUT SHOULD NOT BE RUN ON BUILDING.
     * A NEW INSTALL OF SAGE MAY NOT HAVE THIS ADDRESS IN THE CACHE
     */
//    @Test
//    @Transactional(value = DatabaseConfig.geocoderTxManager)
//    public void getAddressProviderInGeocacheTest() {
//        StreetAddress streetAddress = StreetAddressParser.parseAddress(new Address("100 N HARPERSFIELD RD, JEFFERSON, NY, 12093"));
//        String provider = sqlRegeocacheDao.getProviderOfAddressInCacheIfExists(streetAddress);
//        assertNotNull(provider);
//    }

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void getAllZipsTest() {
        List<String> zips = sqlRegeocacheDao.getAllZips();
        assertTrue(zips.size() >= 1794);
    }
}
