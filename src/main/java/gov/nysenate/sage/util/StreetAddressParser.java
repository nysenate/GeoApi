package gov.nysenate.sage.util;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.StreetAddress;
import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing free-form or semi-parsed addresses into StreetAddress objects.
 * The algorithm does not rely on a specific formatting but supplying commas to delimit the
 * apartment and city from the street can make things easier.
 *
 * Parsing addresses is a core requirement for performing street file look-ups as well as
 * performing geocode caching as they both operate on StreetAddress objects.
 */
public final class StreetAddressParser {
    public static Logger logger = LoggerFactory.getLogger(StreetAddressParser.class);
    public static final Pattern poBoxPattern = Pattern.compile("(?i)P[.]?O[.]?-?\\s*BOX[#-:\\\\+]?\\s*(\\d+)");
    private static final String bldgNumPattern = "^[0-9]+-?[0-9]*[a-zA-Z]?";

    private StreetAddressParser() {}

    /**
     * Parses address and normalizes.
     * @param address Address to parse
     * @return StreetAddress
     */
    // TODO: may need to remove "#"
    public static StreetAddress parseAddress(Address address) {
        var stAddr = new StreetAddress();
        // TODO: need to parse bldgId, street, and internal from addr1 and addr2

//        normalizeStreet
//        return normalizeStreetAddress(stAddr);
        return stAddr;
    }

    public static String normalizeStreet(String street) {
        if (street == null) {
            return null;
        }
        // The following line would remove all numerical suffixes and special characters.
        // This causes problems when matching the street file table. This may adversely affect the geocache table
        return street.replaceAll("[#:;.,']", "").replaceAll("[ -]+", " ").toUpperCase();
    }

    /**
    * Assume that the address begins with a digit, and extract it from the input string.
    * Handle dashes in the building number as well as a possible building character.
    */
    private static String getBldgId(String addressStr) {
        Matcher matcher = Pattern.compile(bldgNumPattern).matcher(addressStr);
        if (matcher.find()) {
            return matcher.group("bldgId");
        }
        return null;
    }

    /**
     * This method takes in a street address and ensures that it is in mixed case
     * For example, W TYPICAL ST NW APT 1S -> W Typical St NW Apt 1S
     *
     * @param address address
     * @return
     */
    public static Address performInitCapsOnAddress(Address address) {
        address.setAddr1(initCapStreetLine(address.getAddr1()));
        address.setAddr2(initCapStreetLine(address.getAddr2()));
        address.setPostalCity( WordUtils.capitalizeFully(address.getPostalCity().toLowerCase()) );
        return address;
    }

    /**
     * Makes the address line init capped as in:
     *  W TYPICAL ST NW APT 1S -> W Typical St NW Apt 1S
     * Some exceptions include unit characters and directionals.
     * @param line String
     * @return String
     */
    private static String initCapStreetLine(String line) {
        /** Perform init caps on the street address */
        line = WordUtils.capitalizeFully(line.toLowerCase());
        /** Ensure unit portion is fully uppercase e.g 2N */
        Pattern p = Pattern.compile("([0-9]+-?[a-z]+[0-9]*)$");
        Matcher m = p.matcher(line);
        if (m.find()) {
            line = m.replaceFirst(m.group().toUpperCase());
        }
        /** Ensure (SW|SE|NW|NE) are not init capped */
        p = Pattern.compile("(?i)\\b(SW|SE|NW|NE)\\b");
        m = p.matcher(line);
        if (m.find()) {
            line = m.replaceAll(m.group().toUpperCase());
        }
        /** Change Po Box to PO Box */
        line = line.replaceAll("Po Box", "PO Box");
        return line;
    }
}
