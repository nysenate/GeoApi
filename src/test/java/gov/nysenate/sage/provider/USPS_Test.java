package gov.nysenate.sage.provider;

import static gov.nysenate.sage.AddressTestBase.*;
import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.service.address.AddressService;
import gov.nysenate.sage.util.FormatUtil;
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
    private static Logger logger = Logger.getLogger(USPS_Test.class);
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
    public void singleAddressValidate_ReturnsAddressResult()
    {
        assertSingleAddressValidation(usps);
    }

    @Test
    public void multipleAddressValidate_ReturnsAddressResult()
    {
        assertMultiAddressValidation(usps);
    }

    @Test
    public void cityStateLookup_ReturnsAddressResult()
    {
        assertCityStateLookup(usps);
    }

    @Test
    public void singleInvalidAddressValidate_SetsIsValidatedFalse()
    {
        assertBadAddressValidate(usps);
    }

    @Test
    public void test()
    {
        FormatUtil.printObject(usps.validate(new Address("200 yellow place", "Rockledge", "FL", "")));
    }

}
