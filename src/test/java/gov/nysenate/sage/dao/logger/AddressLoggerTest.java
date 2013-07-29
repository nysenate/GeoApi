package gov.nysenate.sage.dao.logger;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.util.FormatUtil;
import org.junit.Before;
import org.junit.Test;

public class AddressLoggerTest extends TestBase
{
    private AddressLogger addressLogger;

    @Before
    public void setUp() {
        addressLogger = new AddressLogger();
    }

    @Test
    public void getAddressIdTest() {
        FormatUtil.printObject(addressLogger.getAddressId(new Address("Nyroy Dr", "Troy", "NY", "12180")));
    }
}
