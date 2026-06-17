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
 * Scrapes congressional member data from the house website.
 */
public class CongressScraper {
    private static final Logger logger = LoggerFactory.getLogger(CongressScraper.class);
    private static final String HOUSE_MEM_URL = "https://www.house.gov/representatives";

    public static Map<Long, DistrictMember> getCongressionals() {
        var ret = new HashMap<Long, DistrictMember>();

        try {
            // Each state's representatives are in a separate HTML table.
            // For each state, the reps are in the TBODY section.
            // We select the caption of the table which is the sibling of TBODY
            // From there we get each row and parse what we need
            logger.info("Connecting to " + HOUSE_MEM_URL);
            Document doc = Jsoup.connect(HOUSE_MEM_URL).get();
            Element tableCaption = doc.select("caption#state-new-york").first(); //#housegov_reps_by_state-block_default-1394921112
            Element tableBody = tableCaption.siblingElements().get(1);
            Elements memberRows = tableBody.children();

            for (Element member : memberRows) {
                Elements memberInfo = member.children();
                String districtNumber = memberInfo.getFirst().text();
                long distNum = Long.parseLong( districtNumber.replace("st","").replace("nd","")
                        .replace("rd","").replace("th","") );
                String memberName = "";
                try {
                    memberName = memberInfo.get(1).child(0).text();
                }
                catch (IndexOutOfBoundsException e) {
                    //This also likely means they do not have a url where the senator name usually is.
                    memberName = memberInfo.get(1).text();
                }
                String memberUrl = "";
                try {
                    memberUrl = memberInfo.get(1).child(0).attr("href");
                }
                catch (IndexOutOfBoundsException e) {
                    logger.warn("member {} does not appear to have a URL", memberName);
                }

                ret.put(distNum, new DistrictMember(new MemberInfo(memberName, null, memberUrl, null), null));
            }

            return ret;
        }
        catch (IOException ioe) {
            logger.error("{}", String.valueOf(ioe));
        }
        return ret;
    }
}
