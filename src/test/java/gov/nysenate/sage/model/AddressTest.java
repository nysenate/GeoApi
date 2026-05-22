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
        assertEquals("529 Columbia Turnpike", result.getAddr1());
        assertEquals("Rensselaer", result.getPostalCity());
        assertEquals("NY", result.getState());
    }

    @Test
    public void toStringTest() {
        var address = new Address("1234 Testing Ln", "Test Valley", "T", "12345");
        assertEquals("1234 Testing Ln, Test Valley, T, 12345", address.toString());
    }

    @Test
    public void isEmptyTest() {
        var a = new Address("", "", "", "");
        assertFalse(a.isValid());
    }

    @Test
    public void isEligibleForValidationTest() {
        var eligible1 = new Address("100 N Drive", "Troy", "NY", "12180");
        var eligible2 = new Address("100 N Drive", "", "", "12180");
        var eligible3 = new Address("100 N Drive", "Troy", "NY", "");
        var eligible4 = new Address("100 N Drive", "Troy", "", "");
        var notEligible1 = new Address("100 N Drive", "", "", "");
        var notEligible2 = new Address("100 N Drive", "", "NY", "");
        var notEligible4 = new Address("", "Troy", "NY", "12180");

        assertTrue(eligible1.isValid());
        assertTrue(eligible2.isValid());
        assertTrue(eligible3.isValid());
        assertTrue(eligible4.isValid());
        assertFalse(notEligible1.isValid());
        assertFalse(notEligible2.isValid());
        assertFalse(notEligible4.isValid());
    }
}
