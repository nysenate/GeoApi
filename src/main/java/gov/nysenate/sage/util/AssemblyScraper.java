package gov.nysenate.sage.util;

import gov.nysenate.sage.model.district.DistrictMember;
import gov.nysenate.sage.model.district.MemberInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Scrapes assembly member data from the assembly website
 */
// TODO: can parse full info, including offices and phone numbers, from just https://nyassembly.gov/mem/
public class AssemblyScraper {
    private static final Logger logger = LoggerFactory.getLogger(AssemblyScraper.class);
    private static final String ASSEMBLY_URL = "https://www.nyassembly.gov";

    public static Map<Long, DistrictMember> getAssemblies() {
        var ret = new HashMap<Long, DistrictMember>();

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

            logger.info("Connecting to " + ASSEMBLY_URL);

            Document doc = Jsoup.connect(ASSEMBLY_URL + "/mem/email").get();
            Elements memberList = doc.select("#mem-email-list li"); //#mem-email-list
            for (Element member : memberList) {
                try {
                    Elements memberInfo = member.children();
                    String memberName = memberInfo.get(0).text();
                    String memberUrl = ASSEMBLY_URL + memberInfo.get(0).attr("href");
                    String districtNumber = memberInfo.get(1).text().replace("District","")
                            .replaceAll("st|nd|rd|th","").trim();
                    long distNum = Long.parseLong(districtNumber);

                    ret.put(distNum, new DistrictMember(new MemberInfo(memberName, null, memberUrl, null), null));
                }
                catch (Exception e) {
                    logger.warn(e.getMessage());
                }

            }

            return ret;
        }
        catch (IOException ioe) {
            logger.error("{}", String.valueOf(ioe));
        }
        return ret;
    }
}
