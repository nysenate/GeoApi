package gov.nysenate.sage.dao.logger;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.dao.logger.address.SqlAddressLogger;
import gov.nysenate.sage.model.address.Address;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class AddressLoggerIT extends BaseTests {
    private final Address address = new Address("TEST ADDR","","TEST","NY","99999", "9999");

    @Autowired
    SqlAddressLogger sqlAddressLogger;

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void testInsertAddress() {
        sqlAddressLogger.logAddress(address);
    }

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void testRetrieveAddress() {
        int insertedID = sqlAddressLogger.logAddress(address);
        int retreivedID = sqlAddressLogger.getAddressId(address);
        assertEquals(insertedID, retreivedID);
    }

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void testInsertNullAddress() {
        int insertedID = sqlAddressLogger.logAddress(null);
        assertEquals(0, insertedID);
    }
}
