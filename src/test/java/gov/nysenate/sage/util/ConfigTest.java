package gov.nysenate.sage.util;

import static org.junit.Assert.*;
import gov.nysenate.sage.listener.SageConfigurationListener;
import org.junit.Before;
import org.junit.Test;
import java.util.Observable;
import java.util.Observer;

/** Test the Config implementation using the test.app.properties resource file. *
 * @see Config */

public class ConfigTest
{
    private SageConfigurationListener listener;
    private Config config;

    @Before
    public void setUp() throws Exception
    {
        listener = new SageConfigurationListener();
        config = new Config("test.app.properties", listener);
    }

    @Test
    public void configReturnsCorrectValuesForValidKeys() throws Exception
    {
        /** Check common keys that should not change any time soon */
        assertNotNull( config.getValue("user.public.key") );
    }

    @Test
    public void configReturnsEmptyStringOnInvalidKey()
    {
        assertEquals("", config.getValue("bad key"));
        assertEquals("", config.getValue("unknown.variable.key"));
    }
}
