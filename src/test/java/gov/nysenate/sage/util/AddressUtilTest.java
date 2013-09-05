package gov.nysenate.sage.util;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.StreetAddress;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddressUtilTest
{
    Logger logger = Logger.getLogger(this.getClass());

    @Test
    public void addPunctuationTest()
    {
        Address a = new Address("43 Fairlawn Ave", "Smithtown", "NY", "12108");
        logger.debug(AddressUtil.addPunctuation(a));

    }
}
