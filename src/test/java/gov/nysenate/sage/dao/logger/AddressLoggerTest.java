package gov.nysenate.sage.dao.logger;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.model.address.Address;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class AddressLoggerTest extends TestBase
{
    private AddressLogger addressLogger;

    @Before
    public void setUp() {
        addressLogger = new AddressLogger();
    }

    @Test
    public void getAddressIdTest() {
        assertNotNull( addressLogger.getAddressId(new Address("Nyroy Dr", "Troy", "NY", "12180")));
    }
}
