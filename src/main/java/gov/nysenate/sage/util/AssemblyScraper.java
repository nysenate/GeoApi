package gov.nysenate.sage.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.nysenate.sage.model.district.Assembly;
import org.apache.log4j.Logger;

/**
 * Scrapes assembly member data from the assembly website
 */
public class AssemblyScraper
{
    private static final Logger logger = Logger.getLogger(AssemblyScraper.class);

    static final String ASSEMBLY_URL = "http://assembly.state.ny.us";
    static final String ASSEMBLY_DIRECTORY_URL = ASSEMBLY_URL+"/mem/?sh=email";

    public static List<Assembly> getAssemblies()
    {
        List<Assembly> ret = new ArrayList<>();

        try {
            Pattern p = Pattern.compile("<div class=\"email1\"><a href=\"(.+?)\">(.+?)</a></div>");
            Pattern p2 = Pattern.compile("<div class=\"email2\">(\\d+).*?</div>");

            logger.info("Connecting to " + ASSEMBLY_DIRECTORY_URL);

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new URL(ASSEMBLY_DIRECTORY_URL).openStream()));

            String in;
            while ((in = br.readLine()) != null) {
                Matcher m = p.matcher(in);
                if (m.find()) {
                    logger.info("Fetching assembly member " + m.group(2));
                    Matcher m2 = p2.matcher(br.readLine());
                    if (m2.find()) {
                        Assembly a = new Assembly(Integer.valueOf(m2.group(1)), m.group(2), ASSEMBLY_URL + m.group(1));
                        ret.add(a);
                    }
                }
            }

            br.close();
        }
        catch (IOException ioe) {
            logger.warn(ioe);
        }
        return ret;
    }
}
