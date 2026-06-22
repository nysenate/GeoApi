package gov.nysenate.sage.util;

import gov.nysenate.sage.client.view.address.AddressView;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.district.DistrictMember;
import gov.nysenate.sage.model.district.MemberInfo;
import gov.nysenate.sage.model.district.OfficeInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Scrapes Assembly member data from the Assembly website.
 * <p>
 * The member directory at {@code /mem/} lists every member as a {@code section.mem-item} whose
 * {@code id} is the (zero-padded) district number and which carries the member's name, headshot,
 * e-mail, and one or more office addresses.
 */
public class AssemblyScraper {
    private static final Logger logger = LoggerFactory.getLogger(AssemblyScraper.class);
    private static final String ASSEMBLY_URL = "https://www.nyassembly.gov";

    /** Matches a "City, ST 12345" or "City, ST 12345-6789" line within an office address block. */
    private static final Pattern CITY_STATE_ZIP =
            Pattern.compile("^(.+), NY (\\d{5})(?:-(\\d{4}))?$");

    public static Map<Long, DistrictMember> getAssemblyMembers() throws IOException {
        logger.info("Connecting to {}/mem/", ASSEMBLY_URL);
        Document doc = Jsoup.connect(ASSEMBLY_URL + "/mem/").get();

        var ret = new HashMap<Long, DistrictMember>();
        for (Element member : doc.select("section.mem-item")) {
            try {
                Element nameAnchor = Objects.requireNonNull(member.selectFirst("h3.mem-name a"));
                String displayName = nameAnchor.ownText().trim();
                String url = ASSEMBLY_URL + nameAnchor.attr("href");
                String imageUrl = ASSEMBLY_URL + Objects.requireNonNull(member.selectFirst(".mem-pic img"))
                        .attr("src");
                String email = Objects.requireNonNull(member.selectFirst(".mem-email a")).text().trim();

                long distNum = Long.parseLong(member.id().trim());
                var info = new MemberInfo(getInvertedName(displayName, email), imageUrl, url, email);
                List<OfficeInfo> offices = member.select(".mem-address .full-addr").stream()
                        .map(ele -> parseOffice(ele.html())).toList();

                ret.put(distNum, new DistrictMember(info, offices));
            }
            catch (Exception e) {
                logger.warn("Failed to parse assembly member: {}", e.getMessage());
            }
        }
        return ret;
    }

    protected static String getInvertedName(String name, String email) {
        String trimmedEmail = email.split("@")[0];
        trimmedEmail = trimmedEmail.substring(0, trimmedEmail.length() - 1);
        var trimmedEmailRegex = new StringBuilder();
        for (int i = 0; i < trimmedEmail.length(); i++) {
            trimmedEmailRegex.append(trimmedEmail.charAt(i)).append("[ .']?");
        }
        Pattern emailPattern = Pattern.compile(trimmedEmailRegex.toString(), Pattern.CASE_INSENSITIVE);
        Matcher emailMatcher = emailPattern.matcher(name);
        if (emailMatcher.find()) {
            String lastName = name.substring(emailMatcher.start());
            String restOfName = name.substring(0, emailMatcher.start()).trim();
            return lastName + ", " + restOfName;
        }
        String[] splitName = name.split(" ");
        var invertedNameBuilder = new StringBuilder();
        for (int i = 0; i < splitName.length - 1; i++) {
            invertedNameBuilder.append(splitName[i]).append(" ");
        }
        return splitName[splitName.length - 1] + ", " + invertedNameBuilder.toString().trim();
    }

    /**
     * Parses a single {@code .full-addr} block. Lines are {@code <br>}-separated.
     */
    private static OfficeInfo parseOffice(String addrHtml) {
        List<String> lines = Arrays.stream(addrHtml.split("<br>"))
                .filter(line -> !line.isEmpty()).toList();

        String addr2 = "", city = null, zip5 = null, zip4 = null;
        int cityIdx = -1;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            Matcher m = CITY_STATE_ZIP.matcher(line);
            if (m.matches()) {
                city =  m.group(1).trim();
                zip5 =  m.group(2);
                zip4 =  m.group(3);
                cityIdx = i;
                break;
            }
            else {
                addr2 = line;
            }
        }

        var office = new OfficeInfo();
        var baseAddress = new Address(lines.getFirst(), addr2, city, "NY", zip5, zip4);
        office.setAddress(new AddressView(baseAddress, false));
        office.setName("12248".equals(zip5) ? "Albany Office" : "District Office");
        if (cityIdx != -1) {
            office.setPhone(lines.get(cityIdx + 1));
        }
        return office;
    }
}
