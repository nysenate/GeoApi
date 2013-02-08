package gov.nysenate.sage;

import gov.nysenate.sage.factory.ApplicationFactory;
import org.junit.Before;

/**
 * Sets up common testing dependencies such as the ApplicationFactory instance.
 */
public class TestBase
{
    @Before
    public void setUp()
    {
        ApplicationFactory.buildTestInstances();
    }
}
