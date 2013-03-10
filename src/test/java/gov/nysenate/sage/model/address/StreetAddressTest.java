package gov.nysenate.sage.model.address;

import gov.nysenate.sage.TestBase;
import org.junit.Test;
import static org.junit.Assert.*;

public class StreetAddressTest extends TestBase
{
    @Test
    public void toAddressTest()
    {
        StreetAddress sa = new StreetAddress();
        sa.setBldgNum(185);
        sa.setStreet("79th");
        sa.setStreetType("St");
        sa.setPreDir("E");
        sa.setInternal("Suite 20");
        sa.setLocation("New York");
        sa.setState("NY");
        sa.setZip5("10075");

        Address a = sa.toAddress();
        assertEquals("185 E 79th St", a.getAddr1());
        assertEquals("Suite 20", a.getAddr2());
        assertEquals("New York", a.getCity());
        assertEquals("NY", a.getState());
        assertEquals("10075", a.getZip5());

        /** If internal not found, try bldg char as addr2 */
        sa.setInternal(null);
        sa.setBldgChar("A");
        a = sa.toAddress();
        assertEquals("A", a.getAddr2());

        /** If bldg char not found, try apt as addr2 */
        sa.setBldgChar("");
        sa.setAptNum(11);
        sa.setAptChar("B");
        a = sa.toAddress();
        assertEquals("11B", a.getAddr2());
    }

}
