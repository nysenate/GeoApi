package gov.nysenate.sage.controller.api.address;

import gov.nysenate.sage.MockFilter;
import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.util.Config;
import org.junit.BeforeClass;
import org.junit.Test;

public class ValidateAddressControllerTest extends TestBase
{
    private MockFilter mf;
    private Config config;

    @BeforeClass
    private void setUp()
    {
        config = ApplicationFactory.getConfig();
        mf = new MockFilter();
    }

    @Test
    public void validateControllerWithValidRequest_ReturnsAddressResponse()
    {

    }

    @Test
    public void validateControllerWithInvalidRequest_ReturnsError()
    {

    }

    @Test
    public void validateControllerWithURLRequest_ReturnsAddressResponse()
    {

    }

    @Test
    public void validateControllerWithBodyRequest_ReturnsAddressResponse()
    {

    }


}
