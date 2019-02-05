package gov.nysenate.sage.provider;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.provider.address.USPSAIS;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class USPSAisTest extends BaseTests {

    @Autowired
    USPSAIS uspsais;

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void validateAddressTest()
    {
        assertNotNull(
                uspsais.validate(new Address("200 yellow place", "Rockledge", "FL", "")));
    }
}
