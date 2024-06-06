package gov.nysenate.sage.util;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.StreetAddress;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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
    public static final String SEP = "[ ,]+";

    public static final Set<String> streetTypeSet = AddressDictionary.streetTypeMap.keySet();
    public static final Set<String> highWaySet = AddressDictionary.highWayMap.keySet();
    public static final Set<String> unitSet = AddressDictionary.unitMap.keySet();
    public static final Set<String> dirSet = AddressDictionary.directionMap.keySet();

    public static final String unitRegex = "(" + StringUtils.join(unitSet, "|") + ")";
    public static final Pattern poBoxPattern = Pattern.compile("(?i)P[.]?O[.]?-?\\s*BOX[#-:\\\\+]?\\s*(\\d+)");

    private StreetAddressParser() {}

    /**
     * Parses address and normalizes.
     * @param address Address to parse
     * @return StreetAddress
     */
    public static StreetAddress parseAddress(Address address)
    {
        return parseAddressComponents(address);
    }

    /**
     * Parses address string and normalizes.
     * @param address Address to parse
     * @return StreetAddress
     */
    public static StreetAddress parseAddress(String address)
    {
        return parseAddress(new Address(address));
    }

    /**
     * Applies minor corrections to the StreetAddress and returns a reference to it.
     * @param streetAddr
     * @return normalized StreetAddresss
     */
    public static StreetAddress normalizeStreetAddress(StreetAddress streetAddr)
    {
        /** Fix up towns */
        String town = streetAddr.getLocation();
        if (isset(town)) {
            town = town.replaceFirst("^(TOWN |TOWN OF |CITY |CITY OF |)", "");
            town = town.replaceFirst("(\\(CITY\\)|/CITY)$", "");
            streetAddr.setLocation(town);
        }

        /** Fix up street name */
        String street = streetAddr.getStreetName();
        if (isset(street)) {
            /** The following line would remove all numerical suffixes and special characters.
             *  This causes problems when matching the street file table. This may adversely affect the geocache table
             */
            street = street.replaceFirst("(?<=[0-9])(?:ST|ND|RD|TH)", "");
            street = street.replaceAll("[#:;.,]", "").replaceAll("'", "").replaceAll(" +", " ").replaceAll("-", " ");
            street = normalize(street);
            streetAddr.setStreetName(street);
        }

        return streetAddr;
    }

    /**
    * Delegates to parsing methods based on the Address.isParsed condition.
    * @param addr Address to parse
    * @return parsed StreetAddress
    */
    private static StreetAddress parseAddressComponents(Address addr)
    {
        StreetAddress stAddr = new StreetAddress();

        if (addr.isParsed()) {
            stAddr.setLocation(normalize(addr.getPostalCity()));
            stAddr.setState(normalize(addr.getState()));
            stAddr.setZip5(addr.getZip5());
            stAddr.setZip4(addr.getZip4());

            String addrStr = addr.getAddr1(); // + ((!addr.getAddr2().isEmpty()) ? " " + addr.getAddr2() : "");
            addrStr = extractBldgNum(addrStr, stAddr);
            extractStreet(normalize(addrStr), stAddr);
            if (stAddr.getInternal().isEmpty() && !addr.getAddr2().isEmpty()) {
                stAddr.setInternal(addr.getAddr2());
            }
        }
        else {
            String addrStr = normalize(addr.toString());

            addrStr = extractUSA(addrStr);
            addrStr = extractZip(addrStr, stAddr);
            addrStr = extractState(addrStr, stAddr);
            addrStr = extractBldgNum(addrStr, stAddr);
            extractStreet(addrStr, stAddr);
        }

        return normalizeStreetAddress(stAddr);
    }

    /**
     * Removes 'United States' or a variant from the end of the string
     */
    private static String extractUSA(String addressStr)
    {
        String countyPattern = "(?i)U((nited States( of America)?)|(\\.?S\\.?A?\\.?))$";
        return addressStr.replaceFirst(countyPattern, "").trim();
    }

    /**
    * Extracts the zip and sets it to the supplied StreetAddress
    */
    private static String extractZip(String addressStr, StreetAddress streetAddress)
    {
        String zipPattern = "(?<zip>(?<zip5>(?<![0-9])([-0-9]{5}))([ -](?<zip4>([0-9]{4})))?)[, ]*$";
        Matcher zipMatcher = Pattern.compile(zipPattern).matcher(addressStr);
        if (zipMatcher.find()) {
            streetAddress.setZip5(zipMatcher.group("zip5"));
            streetAddress.setZip4(zipMatcher.group("zip4"));
            logger.debug("Zipcode: " + zipMatcher.group("zip"));
            addressStr = addressStr.replace(zipMatcher.group("zip"), "").trim();
        }
        return addressStr;
    }

    /**
    * Assumes zip code has already been extracted from addressStr
    */
    private static String extractState(String addressStr, StreetAddress streetAddress)
    {
        String stateAbbrPattern = SEP + "(?<state>\\w{2})[, ]*$";
        String stateFullPattern = SEP + "(?<state>\\w+([ ]\\w+)?)[, ]*$";

        /** Check for state abbrev */
        Matcher stateMatcher = Pattern.compile(stateAbbrPattern).matcher(addressStr);
        if (stateMatcher.find()) {
            if (AddressDictionary.stateMap.containsKey(stateMatcher.group("state"))) {
                logger.debug("State: " + stateMatcher.group("state"));
                streetAddress.setState(stateMatcher.group("state"));
                addressStr = addressStr.replaceFirst(stateAbbrPattern, "").trim();
            }
        }
        /** Check if state is written out. Set as the abbreviation if found */
        else {
            stateMatcher = Pattern.compile(stateFullPattern).matcher(addressStr);
            if (stateMatcher.find()) {
                for (Map.Entry<String, String> entry : AddressDictionary.stateMap.entrySet()) {
                    if (stateMatcher.group("state").equalsIgnoreCase(entry.getValue())) {
                        logger.debug("State: " + entry.getKey());
                        streetAddress.setState(entry.getKey());
                        addressStr = addressStr.replaceFirst(entry.getValue() + "[, ]*$", "").trim();
                    }
                }
            }
        }
        return addressStr;
    }

    /**
    * Assume that the address begins with a digit, and extract it from the input string.
    * Handle dashes in the building number as well as a possible building character.
    */
    private static String extractBldgNum(String addressStr, StreetAddress streetAddress)
    {
        String bldgNumPattern = "^(?<bldgNum1>[0-9]+)(-(?<bldgNum2>[0-9]+))?(?<bldgChr>[a-zA-Z])?";
        Matcher matcher = Pattern.compile(bldgNumPattern).matcher(addressStr);
        if (matcher.find()) {
            String bldgNum = ((matcher.group("bldgNum1") != null) ? matcher.group("bldgNum1") : "") +
                             ((matcher.group("bldgNum2") != null) ? matcher.group("bldgNum2") : "");
            String bldgChr = (matcher.group("bldgChr") != null ? matcher.group("bldgChr") : "");
            logger.debug("BldgNum: " + bldgNum);
            streetAddress.setBldgNum(Integer.parseInt(bldgNum));
            streetAddress.setBldgChar(bldgChr);
            addressStr = addressStr.replaceFirst(bldgNumPattern, "").trim();
        }
        return addressStr;
    }

    /**
    * Despite the name, this method actually extracts the streetName, streetType, internal, location,
    * pre/post directionals, and possible PO Boxes.
    */
    public static String extractStreet(String addressStr, StreetAddress streetAddress)
    {
        /** Replace new lines/tabs with spaces */
        addressStr = addressStr.replaceAll("[\n\t]", " ");

        String[] addrParts = addressStr.split(",");
        if (addrParts.length >= 1) {
            LinkedList<String> stParts = new LinkedList<>(Arrays.asList(addrParts[0].split(" ")));
            normalize(stParts);

            /** Look for pre-directional */
            String preDir = stParts.get(0);
            if (dirSet.contains(preDir)) {
                logger.debug("PreDir: " + AddressDictionary.directionMap.get(stParts.get(0)));
                streetAddress.setPreDir(AddressDictionary.directionMap.get(stParts.get(0)));
                stParts.pop();
            }

            /** Handle the easy case: Street, Apartment, Location */
            if (addrParts.length == 3) {
                streetAddress.setInternal(normalize(addrParts[1]));
                logger.debug("Internal: " + streetAddress.getInternal());
                if (!isset(streetAddress.getLocation())) {
                    streetAddress.setLocation(addrParts[2]);
                    logger.debug("Location: " + streetAddress.getLocation());
                }
            }
            else {
                LinkedList<String> intParts;
                /** Handle the following cases:
                 *  Street, Apartment
                 *  Street, Location
                 */
                if (addrParts.length == 2) {
                    intParts = new LinkedList<>(stParts);
                    String internal = extractInternal(intParts, stParts);
                    if (!internal.isEmpty()) {
                        streetAddress.setInternal(internal);
                        logger.debug("Internal: " + streetAddress.getInternal());
                        if (!isset(streetAddress.getLocation())) {
                            streetAddress.setLocation(normalize(addrParts[1]));
                            logger.debug("Location: " + streetAddress.getLocation());
                        }
                    }
                    else {
                        intParts = new LinkedList<>(Arrays.asList(addrParts[1].split(" ")));
                        normalize(intParts);
                        internal = extractInternal(intParts, null);
                        if (!internal.isEmpty()) {
                            streetAddress.setInternal(internal);
                            logger.debug("Internal: " + streetAddress.getInternal());
                        }
                        else {
                            if (!isset(streetAddress.getLocation())) {
                                streetAddress.setLocation(normalize(addrParts[1]));
                                logger.debug("Location: " + streetAddress.getLocation());
                            }
                        }
                    }
                }
                else {
                    intParts = new LinkedList<>(stParts);
                    String internal = extractInternal(intParts, stParts);

                    /** If an internal is found, it might be possible that the location is also included.
                     *  Here we determine the bounds of the internal component and set whatever is to the right
                     *  of that bound as the location */
                    if (!internal.isEmpty()) {
                        LinkedList<String> intList = new LinkedList<>(Arrays.asList(internal.split(" ")));
                        int loc = -1;
                        boolean single = false;
                        for (int i = 0; i < intList.size(); i++) {
                            if (intList.get(i).matches(unitRegex + "#?")) {
                                loc = i;
                                break;
                            }
                            else if (intList.get(i).matches(unitRegex + "([-#a-zA-Z0-9]+)?")) {
                                loc = i;
                                single = true;
                                break;
                            }
                        }
                        if (loc > -1 && loc != intList.size() - 1) {
                            if (single) {
                                streetAddress.setInternal(StringUtils.join(intList.subList(0, loc + 1), " "));
                                if (!isset(streetAddress.getLocation())) {
                                    streetAddress.setLocation(StringUtils.join(intList.subList(loc + 1, intList.size()), " "));
                                }
                            }
                            else {
                                if (intList.get(loc + 1).equals("#")) {
                                    streetAddress.setInternal(StringUtils.join(intList.subList(0, loc + 3), " "));
                                    if (!isset(streetAddress.getLocation())) {
                                        streetAddress.setLocation(StringUtils.join(intList.subList(loc + 3, intList.size()), " "));
                                    }
                                }
                                else {
                                    streetAddress.setInternal(StringUtils.join(intList.subList(0, loc + 2), " "));
                                    if (!isset(streetAddress.getLocation())) {
                                        streetAddress.setLocation(StringUtils.join(intList.subList(loc + 2, intList.size()), " "));
                                    }
                                }
                            }
                            logger.debug("Internal: " + streetAddress.getInternal());
                            logger.debug("Location: " + streetAddress.getLocation());
                        }
                    }
                }
            }

            /** Look for street type. */
            LinkedList<String> stPartsClone = new LinkedList<>(stParts),
                               stTypeList = new LinkedList<>(),
                               locList = new LinkedList<>();


            /** Try to match a street/highway type by iteratively shrinking the input from the right */
            while (stTypeList.isEmpty() && stPartsClone.size() > 1) {
                stTypeList = extractStreetType(stPartsClone, streetAddress);
                if (stTypeList.isEmpty() && stPartsClone.size() > 1) {
                    locList.push(stPartsClone.removeLast());
                }
            }

            /** If street type was found and there was more after it, check for postDir and location */
            if (!stTypeList.isEmpty() && !locList.isEmpty()) {

                /** Look for post-directional */
                String postDir = locList.getFirst();
                if (dirSet.contains(postDir)) {
                    streetAddress.setPostDir(AddressDictionary.directionMap.get(postDir));
                    logger.debug("PostDir: " + streetAddress.getPostDir());
                    stParts.remove(postDir);
                    locList.removeFirst();
                }

                /** Anything remaining is assumed to be the location if not already set */
                if (!locList.isEmpty() && !isset(streetAddress.getLocation())) {
                    streetAddress.setLocation(StringUtils.join(locList, " "));
                    logger.debug("Location due to street type search: " + streetAddress.getLocation());
                    stParts.removeAll(locList);
                }
            }

            /** Remove the street type from the street parts list */
            stParts.removeAll(stTypeList);

            /** Check for PO BOX addresses */
            String streetName = StringUtils.join(stParts, " ");
            Matcher m = poBoxPattern.matcher(streetName);
            if (m.find()) {
                streetAddress.setPoBox(m.group(1));
                logger.debug("PO BOX: " + streetAddress.getPoBox());
                if (!isset(streetAddress.getLocation())) {
                    streetAddress.setLocation(streetName.replace(m.group(0), ""));
                    logger.debug("Location: " + streetAddress.getLocation());
                }
            }
            else {
                /** Highway street names should be a single word. Any excess should be the location */
                if (streetAddress.isHwy() && !isset(streetAddress.getLocation()) && !streetName.isEmpty()) {
                    LinkedList<String> streetNameList = new LinkedList<>(Arrays.asList(streetName.split(" ")));
                    streetAddress.setStreetName(streetNameList.get(0));
                    if (!isset(streetAddress.getLocation())) {
                        streetAddress.setLocation(StringUtils.join(streetNameList.subList(1, streetNameList.size()), " "));
                        logger.debug("Location: " + streetAddress.getLocation());
                    }
                    logger.debug("StreetName: " + streetAddress.getStreetName());
                }
                else {
                    String[] streetNameParts = streetName.trim().split(" ");
                    if (streetNameParts.length == 2 && streetNameParts[1].matches("[0-9]+[a-zA-Z]?")) {
                        streetAddress.setStreetName(streetNameParts[0]);
                        streetAddress.setInternal(streetNameParts[1]);
                    }
                    else {
                        streetAddress.setStreetName(streetName);
                    }

//                    streetAddress.setStreetName(streetName);
                    logger.debug("StreetName: " + streetAddress.getStreetName());
                }
            }

            /** If a 'street' was found but it doesn't have a building number and a street type, it's probably the location. */
            if (streetAddress.getStreetType().isEmpty() && streetAddress.getBldgNum() == null
                && streetAddress.getLocation().isEmpty() && !streetAddress.getStreet().isEmpty()) {
                streetAddress.setLocation(streetAddress.getStreet());
                streetAddress.setStreetName("");
            }
        }

        return addressStr;
    }

    /**
    * Look for a street or highway type designator in the list of street parts provided
    * @param sts  List of street level words
    * @return List of words that comprise the street type or empty list if nothing matched
    */
    private static LinkedList<String> extractStreetType(LinkedList<String> sts, StreetAddress streetAddress)
    {
        String streetType = null;
        /** Look for regular street types. Street types have street names before type. */
        LinkedList<String> sList = new LinkedList<>(sts);
        while (!sList.isEmpty()) {
            String type = StringUtils.join(sList, " ");
            if (streetTypeSet.contains(type)) {
                streetType = normalize(AddressDictionary.streetTypeMap.get(type));
                streetAddress.setStreetType(streetType);
                logger.debug("StreetType: " + streetAddress.getStreetType());
                break;
            }
            else {
                sList.pop();
            }
        }

        /** Look for highway type. Highway types have street names after street type.
         *  This essentially does the same search above but in reverse. */
        if (streetType == null) {
            sList = new LinkedList<>(sts);
            while (!sList.isEmpty()) {
                String type = StringUtils.join(sList, " ");
                if (highWaySet.contains(type)) {
                    streetAddress.setStreetType(normalize(AddressDictionary.highWayMap.get(type)));
                    streetAddress.setHwy(true);
                    logger.debug("HighwayType: " + streetAddress.getStreetType());
                    break;
                }
                else {
                    sList.removeLast();
                }
            }
        }
        return sList;
    }

    /**
    * Attempts to find an internal component from the given list of candidates.
    * @param candidates     List of words that are extracted from the street level portion of the address string
    * @param streetList     The overall street level list that could be the same as the candidates list
    *                       If a match for a unit type is found in candidates, it will be removed from streetList.
    * @return String representation of the matched internal component
    */
    private static String extractInternal(LinkedList<String> candidates, LinkedList<String> streetList)
    {
        String internal = "";
        while (!candidates.isEmpty()) {
            String s = candidates.peek();
            if (unitSet.contains(s) || unitSet.contains(s.replace("#", "")) || s.contains("#")) {
                String unit = AddressDictionary.unitMap.get(s);
                /** Remove from original list */
                if (streetList != null) {
                    streetList.removeAll(candidates);
                }
                if (unit != null) {
                    candidates.set(0, unit);
                }
                internal = StringUtils.join(candidates, " ");
                break;
            }
            candidates.pop();
        }
        return internal;
    }

    /**
    * Shorthand for null/empty check
    */
    private static boolean isset(String s)
    {
        return s != null && !s.isEmpty();
    }

    /**
    * Shorthand for performing uppercase and trim on a string.
    */
    private static String normalize(String s)
    {
        if (s != null) {
            return s.toUpperCase().trim();
        }
        return s;
    }

    /**
    * Shorthand for performing uppercase and trim on each string in a list.
    */
    private static void normalize(List<String> list)
    {
        list.replaceAll(StreetAddressParser::normalize);
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
    private static String initCapStreetLine(String line)
    {
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
