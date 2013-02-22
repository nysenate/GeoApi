package gov.nysenate.sage.provider;

import static gov.nysenate.sage.AddressTestBase.*;
import gov.nysenate.sage.TestBase;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test the functionality of the USPS adapter by executing a few sample requests and
 * comparing them to the expected responses.
 */
public class USPS_Test extends TestBase
{
    private Logger logger = Logger.getLogger(USPS_Test.class);
    private USPS usps;

    @Before
    public void setUp()
    {
        try {
            this.usps = new USPS();
        }
        catch(Exception ex){
            System.err.println(ex.getMessage());
        }
    }

    @Test
    public void USPS_SingleAddressValidate_ReturnsAddressResult()
    {
        assertSingleAddressValidation(usps);
    }

    @Test
    public void USPS_MultipleAddressValidate_ReturnsAddressResult()
    {
        assertMultiAddressValidation(usps);
    }

    @Test
    public void USPS_CityStateLookup_ReturnsAddressResult()
    {
        assertCityStateLookup(usps);
    }
}
