package gov.nysenate.sage.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import gov.nysenate.sage.model.district.Assembly;


/**
 * Scrapes assembly member data from the assembly website
 */
public class AssemblyScraper
{
    private static final Logger logger = Logger.getLogger(AssemblyScraper.class);

    private static final String ASSEMBLY_BASE_URL = "https://www.nyassembly.gov";
    private static final String ASSEMBLY_MEM_URL = ASSEMBLY_BASE_URL+"/mem/email/";

    public static List<Assembly> getAssemblies()
    {
        List<Assembly> ret = new ArrayList<>();

        try {
            // Each Assembly member uses two DIVs.  The first DIV contains
            // the URL and full name of the member.  The second DIV contains
            // the district.  The ordinal suffix ("st", "nd", "rd", "th") is
            // stripped from district ordinal.
            Pattern memberPattern = Pattern.compile("<div class=\"email1\">\\s*<a href=\"([^\"]*)\">([^<]*)</a></div>\\s*<div class=\"email2\">(\\d+)(st|nd|rd|th)</div>");

            logger.info("Connecting to " + ASSEMBLY_MEM_URL);

            String html = IOUtils.toString(new URL(ASSEMBLY_MEM_URL));
            Matcher m = memberPattern.matcher(html);

            while (m.find()) {
                String url = ASSEMBLY_BASE_URL + m.group(1);
                String name = m.group(2).trim();
                int district = Integer.parseInt(m.group(3));
                logger.info("Retrieved member [" + name + "], AD=" + district);
                Assembly a = new Assembly(district, name, url);
                ret.add(a);
            }
        }
        catch (IOException ioe) {
            logger.error(ioe);
        }
        return ret;
    } // getAssemblies()
}
