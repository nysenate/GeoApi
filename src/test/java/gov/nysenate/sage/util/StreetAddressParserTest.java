package gov.nysenate.sage.util;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.StreetAddress;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

public class StreetAddressParserTest {

    @Test
    public void unParsedTest() {
        /** Basic */
        //Address address = new Address("205 N 1105 st W Apt 14 Beverly Hills CA 90210-5221");

        /** PreDir with Internal */
        //Address address = new Address("211 South Pearl Street, Nothing, NY 13204");

        /** Post Dir */
        //Address address = new Address("175-90 HILLCREST VLG E #A3, NISKAYUNA, NY 12309-3806");
        //Address address = new Address("175-90 Hillcrest Village East, Niskayuna, NY 12309-3806");
        //Address address = new Address("13830 West WATERPORT CARLTON RD, APT #78, ALBION, NY 14411");
        //Address address = new Address("300 East CENTRAL PARK West APt 2K New York NY 12108");

        //Address address = new Address("3771 w 118th St West, Apartment #4001C, Queens NY 11432");
        //Address address = new Address("17303 SENECA CHASE PARK RD, POOLESVILLE, MD 20837");
        //StreetAddress sa = new StreetAddress(17303, "", "SENECA CHASE PARK", "RD", "", "", "POOLESVILLE", "MD", "20837");

        /** Route/Highway Addresses */
        //Address address = new Address("2613 ROUTE 11 APT 5 LA FAYETTE, NY 13084");
        //Address address = new Address("2613 ROUTE 11-5 LA FAYETTE, NY 13084");
        //Address address = new Address("7967 W STATE HIGHWAY 5, ST JOHNSVILLE NY 13452-3528");
        //Address address = new Address("3851 ROUTE 9 W, HIGHLAND, NY 12528");
        //Address address = new Address("1375 US HIGHWAY 6, Port Jervis, NY 12771");
        //Address address = new Address("9874 W ROUTE 32, FREEHOLD NY 12431-5349");

        /** PO BOX */
        //Address address = new Address("PO BOX 612 CENTEREACH NY 11722");
        //Address address = new Address("PO BOX 612 CENTEREACH NY 11722");
        Address address = new Address("PO BOX 1582 BRIDGEHAMPTON NY 11932-1582");

        /** Edge cases */
        //Address address = new Address("8121 Main Street, Red Creek NY 13143");
        //Address address = new Address("234 State Hwy 45B, NY 12343");
        //Address address = new Address("241 Avenue X, New York 12324-2324");
        //Address address = new Address("385 HOFSTRA UNIV C SQUARE W Unit 516A, Hempstead, NY 11549");
        //Address address = new Address("500 JOSEPH C WILSON BLVD # 272844, ROCHESTER NY 14627");

        FormatUtil.printObject(StreetAddressParser.parseAddress(address));

        //assertStreetAddressesAreEqual(sa, StreetAddressParser.parseAddress(address));
    }

    @Test
    public void edgeCases()
    {

    }

    @Test
    public void semiParsedTest()
    {
        // PreDir with Internal
        Address address = new Address("211 S LOWELL AVE # 1 FL 3", "Syracuse", "NY", "13204");
        System.out.print(StreetAddressParser.parseAddress(address).toStringParsed());
    }

    public void assertStreetAddressesAreEqual(StreetAddress s1, StreetAddress s2)
    {
        assertThat(s1, notNullValue());
        assertThat(s2, notNullValue());
        assertThat("bldgNum", s1.getBldgNum(), is(s2.getBldgNum()));
        assertThat("bldgChr", s1.getBldgChar(), is(s2.getBldgChar()));
        assertThat("preDir", s1.getPreDir(), is(s2.getPreDir()));
        assertThat("streetName", s1.getStreetName(), is(s2.getStreetName()));
        assertThat("streetType", s1.getStreetType(), is(s2.getStreetType()));
        assertThat("internal", s1.getInternal(), is(s2.getInternal()));
        assertThat("location", s1.getLocation(), is(s2.getLocation()));
        assertThat("postDir", s1.getPostDir(), is(s2.getPostDir()));
        assertThat("zip5", s1.getZip5(), is(s2.getZip5()));
        assertThat("zip4", s1.getZip4(), is(s2.getZip4()));
    }


}