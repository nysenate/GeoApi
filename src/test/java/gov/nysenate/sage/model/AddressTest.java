package gov.nysenate.sage.model;

import gov.nysenate.sage.annotation.UnitTest;
import gov.nysenate.sage.model.address.Address;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

@Category(UnitTest.class)
public class AddressTest {

    @Test
    public void testParser() {
        Address result = Address.getAddress("529 Columbia Turnpike, Rensselaer, NY, USA");
        assertEquals("529 COLUMBIA TURNPIKE", result.getAddr1());
        assertEquals("Rensselaer", result.getPostalCity());
        assertEquals("NY", result.getState());
    }

    @Test
    public void toStringTest() {
        var address = new Address("1234 Testing Ln", "Test Valley", "T", "12345");
        assertEquals("1234 Testing Ln, Test Valley, T 12345", address.toString());
    }

    @Test
    public void isEmptyTest() {
        var a = new Address("", "", "", "", "", "");
        assertFalse(a.isValid());
    }

    @Test
    public void isEligibleForValidationTest() {
        var eligibile1 = new Address("100 N Drive", "Troy", "NY", "12180");
        var eligibile2 = new Address("100 N Drive", "", "", "12180");
        var eligibile3 = new Address("100 N Drive", "Troy", "NY", "");
        var notEligibile1 = new Address("100 N Drive", "", "", "");
        var notEligibile2 = new Address("100 N Drive", "", "NY", "");
        var notEligibile3 = new Address("100 N Drive", "Troy", "", "");
        var notEligibile4 = new Address("", "Troy", "NY", "12180");

        assertTrue(eligibile1.isValid());
        assertTrue(eligibile2.isValid());
        assertTrue(eligibile3.isValid());
        assertFalse(notEligibile1.isValid());
        assertFalse(notEligibile2.isValid());
        assertFalse(notEligibile3.isValid());
        assertFalse(notEligibile4.isValid());
    }
}
