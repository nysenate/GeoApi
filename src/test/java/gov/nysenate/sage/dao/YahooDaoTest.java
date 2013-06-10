package gov.nysenate.sage.dao;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.dao.provider.YahooDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.util.FormatUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class YahooDaoTest extends TestBase
{
    private YahooDao yahooDao = new YahooDao();

    @Test
    public void getGeocodedAddressesBatchTest()
    {
        ArrayList<Address> addresses = new ArrayList<>(Arrays.asList(
                new Address("214 8th Street", "", "Troy", "NY", "12180", ""),
                new Address("101 East State Street", "", "Olean", "NY", "14760", ""),
                new Address("2012 E Rivr Road", "", "Olean", "NY", "14760", ""),
                new Address("44 Fairlawn Ave", "Apt 2B", "Albany", "NY", "12203", ""),
                new Address("18 Greenhaven Dr", "", "Port Jefferson Station", "NY", "11776", ""),
                new Address("479 Deer Park AVE", "", "Babylon", "NY", "11702", "")));

        FormatUtil.printObject(yahooDao.getGeocodedAddresses(addresses));
    }
}
