package gov.nysenate.sage.util;

import gov.nysenate.sage.model.address.Address;
import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AddressUtil {
    private AddressUtil() {}

    /**
     * Adds a period to the end of every directional, street type abbreviation, and unit type.
     * @param address
     * @return Punctuated address
     */
    public static Address addPunctuation(Address address) {
        if (address != null && !address.isEmpty()) {
            Set<String> streetTypes = new HashSet<>();
            streetTypes.addAll(AddressDictionary.streetTypeMap.values());
            streetTypes.addAll(AddressDictionary.highWayMap.values());

            String unitAlt = StringUtils.join(AddressDictionary.unitMap.values(), "|");
            String stTypeAlt = StringUtils.join(streetTypes, "|");
            String directionalAlt = StringUtils.join(AddressDictionary.directionMap.values(), "|");

            String addr1 = address.getAddr1();
            String res = addr1;

            if (addr1 != null && !addr1.isEmpty()) {
                Matcher m = Pattern.compile("(?i)(" + unitAlt + ")([ ]*#?[ ]*\\d*-?\\w*)$").matcher(addr1);
                if (m.find()) {
                    String internal = m.group();
                    addr1 = m.replaceFirst("$1.$2");
                }
                Matcher dirM = Pattern.compile("(?i)\\b(" + directionalAlt + ")\\b").matcher(addr1);
                if (dirM.find()) {
                    String postDir = dirM.group();
                    addr1 = dirM.replaceAll("$1.");
                }
                String addr1Rev = StringUtils.reverseDelimited(addr1, ' ');
                Matcher stypeM = Pattern.compile("(?i)\\b(" + stTypeAlt + ")\\b").matcher(addr1Rev);
                if (stypeM.find()) {
                    String streetType = addr1Rev.substring(stypeM.start(), stypeM.end());
                    addr1 = addr1.replaceFirst("\\b" + streetType + "\\b", streetType + ".");
                }
            }
            address.setAddr1(addr1);
        }
        return address;
    }

    public static Address reorderAddress(Address address) {
        if (address == null) {
            return null;
        }
        if (address.isPOBox()) {
            return address;
        }
        Address reorderedAddress = StreetAddressParser.parseAddress(address).toAddress();
        if (reorderedAddress.getState().isEmpty() && !reorderedAddress.isAddressBlank()) {
            reorderedAddress.setState("NY");
        }
        if (address.getId() != null) {
            reorderedAddress.setId(address.getId());
        }
        return reorderedAddress;
    }

    public static Integer parseZip(String zip) {
        try {
            return Integer.parseInt(zip.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    public static String addSuffixToNumber(String numStr) {
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
