package gov.nysenate.sage;

import gov.nysenate.sage.factory.ApplicationFactory;
import org.junit.BeforeClass;

/**
 * Sets up common testing dependencies such as the ApplicationFactory instance.
 */
public class TestBase
{
    /**
     * Loads configuration settings and global dependencies.
     */
    @BeforeClass
    public static void initBase()
    {
        ApplicationFactory.bootstrapTest();
    }
}
