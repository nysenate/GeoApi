package gov.nysenate.sage.model;

import gov.nysenate.sage.annotation.UnitTest;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;

@Category(UnitTest.class)
public class StreetAddressTest {

    @Test
    public void toAddressTest() {
        var awn = new AddressWithoutNum("E 79th St", "New York", 10075);
        var sa = new StreetAddress(awn);
        sa.setBldgId("185");
        sa.setInternal("Suite 20");

        Address a = sa.toAddress();
        assertEquals("185 E 79th", a.getAddr1());
        assertEquals("Suite 20", a.getAddr2());
        assertEquals("New York", a.getPostalCity());
        assertEquals("NY", a.getState());
        assertEquals(10075, a.getZip5().intValue());
    }
}
