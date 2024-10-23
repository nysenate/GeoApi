package gov.nysenate.sage.model;

import gov.nysenate.sage.annotation.UnitTest;
import gov.nysenate.sage.model.address.Address;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

@Category(UnitTest.class)
public class AddressTest {

    @Test
    public void toStringTest()
    {
        Address address = new Address("1234 Testing Ln", "Test Valley", "T", "12345");
        assertEquals("1234 Testing Ln, Test Valley, T 12345", address.toString());
    }

    @Test
    public void isEmptyTest()
    {
        Address a = new Address();
        assertTrue(a.isEmpty());

        a.setAddr1("Test");
        assertFalse(a.isEmpty());
    }

    @Test
    public void isEligibleForValidationTest()
    {
        Address eligibile1 = new Address("100 N Drive", "Troy", "NY", "12180");
        Address eligibile2 = new Address("100 N Drive", "", "", "12180");
        Address eligibile3 = new Address("100 N Drive", "Troy", "NY", "");
        Address notEligibile1 = new Address("100 N Drive", "", "", "");
        Address notEligibile2 = new Address("100 N Drive", "", "NY", "");
        Address notEligibile3 = new Address("100 N Drive", "Troy", "", "");
        Address notEligibile4 = new Address("", "Troy", "NY", "12180");

        assertTrue(eligibile1.isEligibleForUSPS());
        assertTrue(eligibile2.isEligibleForUSPS());
        assertTrue(eligibile3.isEligibleForUSPS());
        assertFalse(notEligibile1.isEligibleForUSPS());
        assertFalse(notEligibile2.isEligibleForUSPS());
        assertFalse(notEligibile3.isEligibleForUSPS());
        assertFalse(notEligibile4.isEligibleForUSPS());
    }
}
