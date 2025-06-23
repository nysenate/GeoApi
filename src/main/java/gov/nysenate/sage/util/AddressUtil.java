package gov.nysenate.sage.util;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.BuildingAddress;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.WordUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AddressUtil {
    private AddressUtil() {}

    /**
     * Adds a period to the end of every directional, street type abbreviation, and unit type.
     * @return Punctuated address
     */
    public static String addr1WithPunct(Address address) {
        if (!address.isValid() || !(address instanceof BuildingAddress bldgAddr)) {
            return address.toString();
        }
        Set<String> streetTypes = new HashSet<>();
        streetTypes.addAll(AddressDictionary.streetTypeMap.values());
        streetTypes.addAll(AddressDictionary.highWayMap.values());

        String unitAlt = String.join("|", AddressDictionary.unitMap.values());
        String stTypeAlt = String.join("|", streetTypes);
        String directionalAlt = String.join("|", AddressDictionary.directionMap.values());

        String addr1 = bldgAddr.getAddr1();

        if (!addr1.isEmpty()) {
            Matcher m = Pattern.compile("(?i)(" + unitAlt + ")( *#? *\\d*-?\\w*)$").matcher(addr1);
            if (m.find()) {
                addr1 = m.replaceFirst("$1.$2");
            }
            Matcher dirM = Pattern.compile("(?i)\\b(" + directionalAlt + ")\\b").matcher(addr1);
            if (dirM.find()) {
                addr1 = dirM.replaceAll("$1.");
            }
            String addr1Rev = StringUtils.reverseDelimited(addr1, ' ');
            Matcher stypeM = Pattern.compile("(?i)\\b(" + stTypeAlt + ")\\b").matcher(addr1Rev);
            if (stypeM.find()) {
                addr1 = StringUtils.reverseDelimited(stypeM.replaceAll("$1."), ' ');
            }
        }
        return addr1;
    }

    /**
     * Makes the address line init capped as in:
     *  W TYPICAL ST NW APT 1S -> W Typical St NW Apt 1S
     * Some exceptions include unit characters and directionals.
     * @param line String
     * @return String
     */
    public static String initCapStreetLine(String line) {
        if (line == null || line.isEmpty()) {
            return line;
        }
        // Perform init caps on the street address
        line = WordUtils.capitalizeFully(line.toLowerCase());
        // Ensure unit portion is fully uppercase e.g. 2N
        Pattern p = Pattern.compile("([0-9]+-?[a-z]+[0-9]*)$");
        Matcher m = p.matcher(line);
        if (m.find()) {
            line = m.replaceFirst(m.group().toUpperCase());
        }
        // Ensure (SW|SE|NW|NE) are not init capped
        p = Pattern.compile("(?i)\\b(SW|SE|NW|NE)\\b");
        m = p.matcher(line);
        if (m.find()) {
            line = m.replaceAll(m.group().toUpperCase());
        }
        return line.replaceAll("(?i)Po Box", "PO Box");
    }

    /**
     * Sometimes, USPS can validate incorrectly without this standardization.
     * E.g. (9151 71 ROAD, 11375) would become (9151 71ST AVE, 11375) without this.
     */
    public static String standardizeStreet(String street) {
        String[] streetParts = street.toUpperCase().split(" ");
        int tempIdx = 0;
        // Can't be too aggressive in correction: "20 STREET" -> "20th STREET", but "ROUTE 20" is correct.
        if (AddressDictionary.directionMap.containsKey(streetParts[0]) && streetParts.length  > 1) {
            tempIdx = 1;
        }
        streetParts[tempIdx] = AddressUtil.addSuffixToNumber(streetParts[tempIdx]);
        tempIdx = streetParts.length - 1;
        streetParts[tempIdx] = AddressDictionary.streetTypeMap.getOrDefault(streetParts[tempIdx], streetParts[tempIdx]);
        return String.join(" ", streetParts);
    }

    private static String addSuffixToNumber(String numStr) {
        try {
            int num = Math.abs(Integer.parseInt(numStr));
            if (num > 10 && num < 14) {
                return numStr + "th";
            }
            return numStr + switch (num%10) {
                case 1 -> "st";
                case 2 -> "nd";
                case 3 -> "rd";
                default -> "th";
            };
        } catch (NumberFormatException ex) {
            return numStr;
        }
    }
}
