package gov.nysenate.sage.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import gov.nysenate.sage.model.district.Congressional;


/**
 * Scrapes congressional member data from the house website.
 */
public class CongressScraper
{
    private static final Logger logger = Logger.getLogger(CongressScraper.class);
    private static final String HOUSE_MEM_URL = "https://www.house.gov/representatives";

    public static List<Congressional> getCongressionals()
    {
        List<Congressional> ret = new ArrayList<Congressional>();

        try {
            // Each state's representatives are in a separate HTML table.
            // For each state, the reps are in the TBODY section.
            // Use a non-greedy match from "state-new-york" to "</tbody>" to
            // grab the New York section.
            Pattern nystatePattern = Pattern.compile(" id=\"state-new-york\".*?</tbody>", Pattern.DOTALL);
            // Each rep is a separate row, with the district, URL, and name.
            Pattern memberPattern = Pattern.compile("<tr>\\s*<td>(\\d+)(st|nd|rd|th)\\s*</td>\\s*<td><a href=\"([^\"]*)\">([^<]*)</a>");

            logger.info("Connecting to " + HOUSE_MEM_URL);

            String html = IOUtils.toString(new URL(HOUSE_MEM_URL));
            Matcher m = nystatePattern.matcher(html);
            if (m.find()) {
                String nyhtml = m.group();
                m = memberPattern.matcher(nyhtml);
                while (m.find()) {
                    int district = Integer.parseInt(m.group(1));
                    String url = m.group(3).trim();
                    String name = m.group(4).trim();
                    logger.info("Retrieved member [" + name + "], CD=" + district);
                    Congressional c = new Congressional(district, name, url);
                    ret.add(c);
                }
            }
            else {
                logger.warn("Unable to find New York section within the U.S. House member webpage");
            }
        }
        catch (IOException ioe) {
            logger.error(ioe);
        }
        return ret;
    }
}
