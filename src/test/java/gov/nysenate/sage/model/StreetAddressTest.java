package gov.nysenate.sage.model;

import gov.nysenate.sage.annotation.UnitTest;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.StreetAddress;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;

@Category(UnitTest.class)
public class StreetAddressTest {

    @Test
    public void toAddressTest() {
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
        assertEquals("185 E 79th", a.getAddr1());
        assertEquals("Suite 20", a.getAddr2());
        assertEquals("New York", a.getPostalCity());
        assertEquals("NY", a.getState());
        assertEquals("10075", a.getZip5());
    }
}
