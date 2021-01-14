package gov.nysenate.sage.util;

import gov.nysenate.sage.model.district.Assembly;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Scrapes assembly member data from the assembly website
 */
public class AssemblyScraper
{
    private static final Logger logger = LoggerFactory.getLogger(AssemblyScraper.class);

    private static final String ASSEMBLY_BASE_URL = "https://www.nyassembly.gov";
    private static final String ASSEMBLY_MEM_URL = ASSEMBLY_BASE_URL+"/mem/email/";

    public static List<Assembly> getAssemblies()
    {
        List<Assembly> ret = new ArrayList<>();

        try {
            //<li
            //<a href="/mem/{mem-url}">{mem-name}</a>
            //<div class="email2"> {dist-number} District </div>
            //<a href="mailto:{mem-email}">{mem-email}</a>
            ///</li>

            // Each Assembly member is in a list item. As can be seen above.
            // The first Anchor contains the member url and the members name.
            // The DIV contains district number of the member.
            // The ordinal suffix ("st", "nd", "rd", "th") is stripped from district ordinal.

            logger.info("Connecting to " + ASSEMBLY_MEM_URL);

            Document doc = Jsoup.connect(ASSEMBLY_MEM_URL).get();
            Elements memberList = doc.select("#mem-email-list li"); //#mem-email-list
            for (Element member : memberList) {

                Elements memberInfo = member.children();
                String memberName = memberInfo.get(0).text();
                String memberUrl = memberInfo.get(0).attr("href");
                String districtNumber = memberInfo.get(1).text().replaceAll("District","").trim();
                Integer distNum = Integer.parseInt( districtNumber.replace("st","").replace("nd","")
                        .replace("rd","").replace("th","") );
//                String memberEmail = memberInfo.get(2).text().trim();

                logger.info("Retrieved member [" + memberName + "], AD=" + distNum);
                Assembly a = new Assembly(distNum, memberName, memberUrl);
                ret.add(a);
            }
        }
        catch (IOException ioe) {
            logger.error("" + ioe);
        }
        return ret;
    }
}
