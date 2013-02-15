package gov.nysenate.sage.service.address;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.adapter.USPS;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

import static org.mockito.Mockito.mock;

public class AddressServiceProvidersTest extends TestBase
{
    @Test
    public void registerDefaultProviderTest() throws Exception
    {
        AddressServiceProviders.registerDefaultProvider(new USPS());
        assertEquals(USPS.class, AddressServiceProviders.newServiceInstance().getClass());
    }

    @Test
    public void registerProviderTest()
    {

    }

    @Test
    public void newServiceInstanceTest_NoArgsReturnsDefault()
    {

    }

    @Test
    public void newServiceInstanceTest_ReturnsMatchingProvider()
    {

    }

    @Test
    public void newServiceInstanceTest_ReturnsNullOnMissingProvider()
    {

    }

    @Test
    public void newServiceInstanceTest_ReturnsDefaultOnMissingProviderWithFallback()
    {

    }







}
