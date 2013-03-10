package gov.nysenate.sage.util;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.StreetAddress;
import org.junit.Test;
import static org.junit.Assert.*;

public class AddressParserTest
{
    @Test
    public void parseAddressTest()
    {
        StreetAddress sa = AddressParser.parseAddress("148 W 37th St New York, NY 10018");
        assertEquals(148, sa.getBldgNum());
        assertEquals("W 37", sa.getStreet());
        assertEquals("10018", sa.getZip5());

        sa = AddressParser.parseAddress("100 Nyroy Drive Apt 1 Troy, NY 12180");
        assertEquals(100, sa.getBldgNum());
        assertEquals("NYROY DR", sa.getStreet());
        assertEquals("12180", sa.getZip5());

        sa = AddressParser.parseAddress("2025 County Road 4, Stanley NY 14561");
        assertEquals(2025, sa.getBldgNum());
        assertEquals("COUNTY RD 4", sa.getStreet());
        assertEquals("14561", sa.getZip5());

        sa = AddressParser.parseAddress("479 Deer Park Ave, Babylon NY 11702");
        assertEquals(479, sa.getBldgNum());
        assertEquals("DEER", sa.getStreet());  // Doesn't get Park Ave
        assertEquals("11702", sa.getZip5());
    }
}
