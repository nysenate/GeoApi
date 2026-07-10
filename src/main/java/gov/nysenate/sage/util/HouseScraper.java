package gov.nysenate.sage.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gov.nysenate.sage.client.view.address.AddressView;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.district.DistrictMember;
import gov.nysenate.sage.model.district.MemberInfo;
import gov.nysenate.sage.model.district.OfficeInfo;
import gov.nysenate.sage.model.geo.Point;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Scrapes congressional member data from the House.
 */
public class HouseScraper {
    private static final Logger logger = LoggerFactory.getLogger(HouseScraper.class);
    private static final String HOUSE_MEM_URL = "https://www.house.gov/representatives";
    private static final String HOUSE_XML_URL = "https://clerk.house.gov/xml/lists/MemberData.xml";
    // Headshots are keyed by Bioguide ID.
    private static final String HEADSHOT_URL = "https://bioguide.congress.gov/photo/%s.jpg";

    // The Clerk feed only carries the DC office: district offices come from this community-maintained dataset.
     private static final String DISTRICT_OFFICES_URL =
            "https://unitedstates.github.io/congress-legislators/legislators-district-offices.json";

    // We can wait longer for this data to return.
    private static final int DISTRICT_OFFICES_READ_TIMEOUT = 30000;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // Expand the building code to the building name.
    private static final Map<String, String> OFFICE_BUILDING_NAMES = Map.of(
            "CHOB", "Cannon House Office Building",
            "LHOB", "Longworth House Office Building",
            "RHOB", "Rayburn House Office Building");

    /**
     * Parses New York House members from the Clerk's {@code MemberData.xml} feed.
     * This source provides the member's Washington DC office and a
     * Bioguide-ID-derived headshot URL. A separate function gets the member's website link, and
     * the member's in-state district offices are merged in from {@link #getDistrictOffices()}.
     */
    public static Map<Long, DistrictMember> getHouseMembers() throws IOException {
        Map<Long, String> memberLinks = getHouseMemberLinks();
        Multimap<String, OfficeInfo> offices = getDistrictOffices();
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
            String bioguideID = tagText(info, "bioguideID");
            String imageUrl = bioguideID == null ? null : HEADSHOT_URL.formatted(bioguideID);

            String websiteLink = memberLinks.get(distNum);
            String nameStart = tagText(info, "firstname");
            String middleName =  tagText(info, "middlename");
            if (!StringUtils.isBlank(middleName)) {
                nameStart += ' ' + middleName;
            }
            String nameEnd = tagText(info, "lastname");
            String suffix = tagText(info, "suffix");
            if (!StringUtils.isBlank(suffix)) {
                nameEnd += ' ' + suffix;
            }
            var memberInfo = new MemberInfo(nameStart, nameEnd, imageUrl, websiteLink, null);

            offices.get(bioguideID).add(parseOffice(info)); // Washington DC office
            ret.put(distNum, new DistrictMember(memberInfo, offices.get(bioguideID)));
        }
        return ret;
    }

    /**
     * Fetches every member's district offices, keyed by Bioguide ID.
     */
    private static Multimap<String, OfficeInfo> getDistrictOffices() throws IOException {
        logger.info("Getting Congressional district offices...");
        Multimap<String, OfficeInfo> ret = ArrayListMultimap.create();
        JsonNode root = OBJECT_MAPPER.readTree(
                UrlRequest.getResponseFromUrl(DISTRICT_OFFICES_URL, DISTRICT_OFFICES_READ_TIMEOUT));
        for (JsonNode legislator : root) {
            String bioguideID = text(legislator.path("id"), "bioguide");
            if (bioguideID == null) {
                continue;
            }
            for (JsonNode office : legislator.path("offices")) {
                if ("NY".equals(text(office, "state"))) {
                    ret.put(bioguideID, parseDistrictOffice(office));
                }
            }
        }
        return ret;
    }

    /** Builds a single district office from a {@code legislators-district-offices} entry. */
    private static OfficeInfo parseDistrictOffice(JsonNode node) {
        // building and suite are both optional; keep whichever are present as the second address line.
        String addr2 = Stream.of(text(node, "building"), text(node, "suite"))
                .filter(Objects::nonNull).collect(Collectors.joining(", "));
        String[] zipSplit = Objects.toString(text(node, "zip"), "").split("-");
        var address = new Address(text(node, "address"), addr2, text(node, "city"),
                "NY", zipSplit[0], zipSplit.length > 1 ? zipSplit[1] : null);
        var office = new OfficeInfo();
        office.setAddress(new AddressView(address, false));
        office.setName("District Office");
        office.setPhone(text(node, "phone"));
        // The feed is pre-geocoded, so set the point directly to avoid re-geocoding the address.
        String lat = text(node, "latitude"), lon = text(node, "longitude");
        if (lat != null && lon != null) {
            office.setPoint(new Point(lat, lon));
        }
        return office;
    }

    /** Returns the trimmed text of {@code field} on {@code parent}, or {@code null} if absent/null. */
    private static String text(JsonNode parent, String field) {
        JsonNode node = parent.get(field);
        return node == null || node.isNull() ? null : node.asText();
    }

    private static Map<Long, String> getHouseMemberLinks() {
        Document doc;
        logger.info("Getting House member links...");
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
        String addr1 = OFFICE_BUILDING_NAMES.getOrDefault(building, building);
        String officeRoom = tagText(info, "office-room");
        if (!StringUtils.isBlank(officeRoom)) {
            addr1 = officeRoom + ' ' + addr1;
        }

        var address = new Address(addr1, null, "Washington", "DC",
                tagText(info, "office-zip"), tagText(info, "office-zip-suffix"));
        var office = new OfficeInfo();
        office.setAddress(new AddressView(address, false));
        office.setName("DC Office");
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
