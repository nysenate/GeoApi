package gov.nysenate.sage.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.util.Config;


public class ApplicationFactoryTest extends TestBase
{
    @Test
    public void test() throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        Logger logger = LoggerFactory.getLogger(ApplicationFactoryTest.class);
        Config config = ApplicationFactory.getConfig();
        String addressProviders = config.getValue("district.providers");
        StringTokenizer stringTokenizer = new StringTokenizer(addressProviders, "|");

        List<String> aspList = new ArrayList<>();
        while (stringTokenizer.hasMoreTokens()) {
            Object obj = stringTokenizer.nextElement();
            if (obj != null) {
                aspList.add(obj.toString().trim());
            }
        }

        for (String s : aspList) {
            logger.trace(s + " " + config.getValue(s + ".class"));
            Class.forName("gov.nysenate.sage.provider." + config.getValue(s + ".class")).newInstance();
        }

    }
}
