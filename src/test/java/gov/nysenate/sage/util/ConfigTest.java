package gov.nysenate.sage.util;

import static org.junit.Assert.*;
import org.junit.Test;

/** Test the Config implementation.
 *  It's hard to test singleton classes so this is just to check that the
 *  property file is being read successfully.
 * @see Config*/

public class ConfigTest {

    @Test
    public void configReturnsCorrectValuesForValidKeys(){
        /** Check common keys that should not change any time soon */
        assertEquals("com.mysql.jdbc.Driver", Config.read("db.driver"));
        assertEquals("nysenate.gov", Config.read("nysenate.domain"));
    }

    @Test
    public void configReturnsEmptyStringOnInvalidKey(){
        assertEquals("", Config.read("invalid.key"));
    }

}
