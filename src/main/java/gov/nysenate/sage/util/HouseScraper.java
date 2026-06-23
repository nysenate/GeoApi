package gov.nysenate.sage.util;

import gov.nysenate.sage.client.view.address.AddressView;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.district.DistrictMember;
import gov.nysenate.sage.model.district.MemberInfo;
import gov.nysenate.sage.model.district.OfficeInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Scrapes congressional member data from the House.
 */
public class HouseScraper {
    private static final Logger logger = LoggerFactory.getLogger(HouseScraper.class);
    private static final String HOUSE_MEM_URL = "https://www.house.gov/representatives";

    /** The Clerk's authoritative member feed; one {@code <member>} per House seat, all states. */
    private static final String HOUSE_XML_URL = "https://clerk.house.gov/xml/lists/MemberData.xml";
    /** Headshots are keyed by Bioguide ID; this is the official Biographical Directory image. */
    private static final String HEADSHOT_URL = "https://bioguide.congress.gov/photo/%s.jpg";

    // Expand the building code to the building name.
    private static final Map<String, String> OFFICE_BUILDING_NAMES = Map.of(
            "CHOB", "Cannon House Office Building",
            "LHOB", "Longworth House Office Building",
            "RHOB", "Rayburn House Office Building");

    /**
     * Parses New York House members from the Clerk's {@code MemberData.xml} feed.
     * This source provides the member's Washington DC office and a
     * Bioguide-ID-derived headshot URL. A separate function gets the member's website link.
     */
    public static Map<Long, DistrictMember> getHouseMembers() throws IOException {
        Map<Long, String> memberLinks = getHouseMemberLinks();
        logger.info("Connecting to {}", HOUSE_XML_URL);
        Document doc = Jsoup.connect(HOUSE_XML_URL).parser(Parser.xmlParser()).maxBodySize(0).get();

        var ret = new HashMap<Long, DistrictMember>();
        for (Element member : doc.select("member")) {
            // statedistrict is the postal code followed by the zero-padded district, e.g. "NY01".
            String stateDistrict = Objects.requireNonNull(tagText(member, "statedistrict"));
            if (!stateDistrict.startsWith("NY")) {
                continue;
            }
            long distNum = Long.parseLong(stateDistrict.substring(2));
            Element info = Objects.requireNonNull(member.selectFirst("member-info"));
            String imageID = tagText(info, "bioguideID");
            String imageUrl = imageID == null ? null : HEADSHOT_URL.formatted(imageID);

            String websiteLink = memberLinks.get(distNum);
            // namelist is already in inverted form
            String[] nameParts = Objects.requireNonNull(tagText(info, "namelist")).split(",", 2);
            var memberInfo = new MemberInfo(nameParts[0], nameParts[1], imageUrl, websiteLink, null);
            ret.put(distNum, new DistrictMember(memberInfo, List.of(parseOffice(info))));
        }
        return ret;
    }

    private static Map<Long, String> getHouseMemberLinks() {
        Document doc;
        logger.info("Connecting to " + HOUSE_MEM_URL);
        try {
            doc = Jsoup.connect(HOUSE_MEM_URL).get();
        }
        catch (IOException ioe) {
            logger.error("{}", String.valueOf(ioe));
            return Map.of();
        }
        Element tableCaption = doc.select("caption#state-new-york").first();
        Element tableBody = Objects.requireNonNull(tableCaption).siblingElements().get(1);
        Elements memberRows = tableBody.children();

        var ret = new HashMap<Long, String>();
        for (Element member : memberRows) {
            long distNum = Long.parseLong(member.child(0).text().replaceFirst("\\D.*", ""));
            ret.put(distNum, member.child(1).child(0).attr("href"));
        }
        return ret;
    }

    /** Builds the Washington DC office from the data in the feed. */
    private static OfficeInfo parseOffice(Element info) {
        String building = tagText(info, "office-building");
        // Expand the building code to its full name where known, else keep the raw code.
        String buildingName = OFFICE_BUILDING_NAMES.getOrDefault(building, building);

        String name = OFFICE_BUILDING_NAMES.get(building);
        var address = new Address(buildingName, tagText(info, "office-room"), "Washington", "DC",
                tagText(info, "office-zip"), tagText(info, "office-zip-suffix"));
        var office = new OfficeInfo();
        office.setAddress(new AddressView(address, false));
        office.setName(name);
        office.setPhone(tagText(info, "phone"));
        return office;
    }

    private static String tagText(Element parent, String tag) {
        Element ele = parent.selectFirst(tag);
        if (ele == null) {
            logger.warn("No tag found for {}", tag);
            return null;
        }
        return ele.text().trim();
    }
}
