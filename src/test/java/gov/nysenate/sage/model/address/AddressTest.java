package gov.nysenate.sage.model.address;

import gov.nysenate.sage.TestBase;
import org.junit.Test;
import static org.junit.Assert.*;

public class AddressTest
{
    @Test
    public void cloneTest()
    {
        Address a1 = new Address("Something");
        Address a2 = a1.clone();

        a1.setAddr1("Else");
        assertNotEquals(a1.toString(), a2.toString());
    }

    @Test
    public void toStringTest()
    {
        Address address = new Address("1234 Testing Ln", "Test Valley", "T", "12345");
        assertEquals("1234 Testing Ln, Test Valley, T 12345", address.toString());

        address.setAddr2("APT T");
        address.setZip4("6789");
        assertEquals("1234 Testing Ln APT T, Test Valley, T 12345-6789", address.toString());

        Address justAddr1 = new Address("Just addr1");
        assertEquals("Just addr1", justAddr1.toString());

        Address noCity = new Address("1234 Testing Ln", "", "", "T", "12345", "6789");
        assertEquals("1234 Testing Ln, T 12345-6789", noCity.toString());

        Address noCityOrState = noCity;
        noCityOrState.setCity("");
        noCityOrState.setState("");
        assertEquals("1234 Testing Ln, 12345-6789", noCityOrState.toString());

        Address noAddr1 = new Address("", "Test Valley", "T", "12345");
        assertEquals("Test Valley, T 12345", noAddr1.toString());

        Address noState = new Address("1234 Testing Ln", "Testing Valley", "", "12345");
        assertEquals("1234 Testing Ln, Testing Valley, 12345", noState.toString());

        Address justState = new Address();
        justState.setState("NY");
        assertEquals("NY", justState.toString());

        Address shortZip = new Address("Something", "", "City", "NJ", "8540", "5632");
        System.out.println(shortZip.toString());
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
    public void isParsedTest()
    {
        Address a = new Address();
        a.setAddr1("Test");
        a.setCity("Test Valley");
        assertTrue(a.isParsed());

        a.setCity("");
        assertFalse(a.isParsed());
    }

    @Test
    public void zipTest()
    {
        Address a = new Address();
        a.setZip5("1132");

        assertEquals("01132", a.getZip5());
    }
}
