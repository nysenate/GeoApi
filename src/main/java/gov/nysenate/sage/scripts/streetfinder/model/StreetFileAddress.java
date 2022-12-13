package gov.nysenate.sage.scripts.streetfinder.model;

import gov.nysenate.sage.util.AddressDictionary;
import org.apache.logging.log4j.util.Strings;

import java.util.*;
import java.util.regex.Pattern;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

/**
 * Represents a Street Address for the StreetFinder database/parsers
 * It also contains helping methods/formatting help for the StreetFinder Parsers
 */
public class StreetFileAddress {
    private static final Set<StreetFileField> mustNotBeEmpty = EnumSet.of(ELECTION_CODE, WARD,
            CONGRESSIONAL, SENATE, ASSEMBLY, CLEG, VILLAGE, FIRE, CITY_COUNCIL, TOWN_CODE, BOE_TOWN_CODE, COUNTY_CODE);
    private static final Set<StreetFileField> mustBeNumeric = EnumSet.of(ELECTION_CODE, CONGRESSIONAL, SENATE, ASSEMBLY, CITY_COUNCIL);
    private final StreetFinderBuilding primaryBuilding = new StreetFinderBuilding();
    // Really a range for apartment buildings. Only used in Suffolk.
    protected final StreetFinderBuilding secondaryBuilding = new StreetFinderBuilding();
    private String preDirection = "";
    private String postDirection = "";
    private String streetSuffix = "";
    protected final Map<StreetFileField, String> fieldMap = new EnumMap<>(StreetFileField.class);
    private static final Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    /**
     * Default Constructor that sets all fields (except for pre-Direction, post-Direction, and Street Suffix because those are appended to the Street Name)
     * to \N which the default for blank in the geoapi database
     */
    public StreetFileAddress() {
        put(STATE, "NY");
    }

    /**
     * Converts the StreetFinderAddress Object into a String in the format needed for the SQL file
     * @return Object in String form
     */
    public String toStreetFileForm() {
        put(STREET, (preDirection + get(STREET) + " " + streetSuffix).trim());
        // TODO: in the original code, this is always empty
        fieldMap.remove(COUNTY_CODE);
        List<String> fieldList = new ArrayList<>();
        boolean beforeBuildings = true;
        for (var fieldType : StreetFileField.values()) {
            if (beforeBuildings && fieldType.isAfterBuildings()) {
                fieldList.addAll(primaryBuilding.getData());
                fieldList.addAll(secondaryBuilding.getData());
                beforeBuildings = false;
            }
            fieldList.add(get(fieldType));
        }
        return String.join("\t", fieldList) + "\n";
    }

    public void put(StreetFileField type, String value) {
        if (type == ELECTION_CODE && value.contains("-")) {
            return;
        }
        if ((!mustNotBeEmpty.contains(type) || !value.isEmpty()) &&
                (!mustBeNumeric.contains(type) || isNumeric(value))) {
            fieldMap.put(type, value);
        }
    }

    protected String get(StreetFileField type) {
        return fieldMap.getOrDefault(type, "\\N");
    }

    // TODO: The street may have a pre or post direction. Perhaps should be normalized with AddressDictionary.directionMap()?
    public void normalize() {
        setPreDirection(preDirection.trim().toUpperCase());
        setStreet(get(STREET).trim().toUpperCase());
        setStreetSuffix(streetSuffix.trim().toUpperCase());
        put(TOWN, get(TOWN).trim().toUpperCase());
    }

    public void setBuilding(boolean isLow, String data) {
        if (isLow) {
            primaryBuilding.setLow(data);
        }
        else {
            primaryBuilding.setHigh(data);
        }
    }

    /**
     * Sets the pre-Direction. Should be in the standard form of N, E, S, W
     * @param preDirection
     */
    public void setPreDirection(String preDirection) {
        if (!preDirection.isBlank()) {
            //Add a space onto the end because it is added to the StreetName when put in the file
            this.preDirection = preDirection + " ";
        }
    }

    /**
     * Sets the post-Direction. Should be in the standard form of N, E, S, W
     * @param postDirection
     */
    public void setPostDirection(String postDirection) {
        if (postDirection != null)
            this.postDirection = postDirection;
    }

    /**
     * Sets the Street Name. Checks the Street Name for possible conversions into the format
     * Used by USPS
     * @param street
     */
    public void setStreet(String street) {
        if (Strings.isEmpty(street)) {
            return;
        }
        String tempStreet = "";
        String[] string = street.split("\\s+");
        // Check for matches in AddressDictionary highWayMap
        for (String s : string) {
            tempStreet += s;
            //if tempStreet is found as a key then set tempStreet to the keys value
            if (AddressDictionary.highWayMap.containsKey(tempStreet)) {
                tempStreet = AddressDictionary.highWayMap.get(tempStreet).toUpperCase();
            }
            tempStreet += " ";
        }
        put(STREET, tempStreet.trim());
    }

    /**
     * Accessor Method
     * @return
     */
    public String getStreet() {
        return get(STREET);
    }

    /**
     * Sets the Street Suffix. This is appended to the end of the Street Name when converted to file form
     * Also checks for conversions to USPS standard format
     * @param streetSuffix
     */
    public void setStreetSuffix(String streetSuffix) {
        if (streetSuffix != null) {
            //Trim off possible . from suffix ex. "Ext."
            String string = streetSuffix.replace(".", "");
            //First check the streetTypeMap
            //If not found in either hashMap then just set to the regular string
            if (AddressDictionary.streetTypeMap.containsKey(string.toUpperCase())) {
                this.streetSuffix = AddressDictionary.streetTypeMap.get(string.toUpperCase()).toUpperCase();
                //Then check highWayMap
            } else
                this.streetSuffix = AddressDictionary.highWayMap.getOrDefault(string.toUpperCase(), string).toUpperCase();
        }
    }

    /**
     * Accessor method
     * @return
     */
    public String getStreetSuffix() {
        return streetSuffix;
    }

    public static String cleanBuilding(String bldg) {
        return bldg.replaceAll("1/2","").replaceAll("1/4","");
    }

    /**
     * Sets the building Parity. It is assumed to be in the form of "ODDS", "EVENS", or "ALL"
     * @param parity
     */
    public void setBldgParity(String parity) {
        primaryBuilding.setParity(parity);
    }

    /**
     * Sets the townCode. Checks if the code is numbers or if the code is characters.
     * If it is numbers then it is passes to setBoeTownCode
     * @param townCode - Can either be ints or a combination of characters and numbers
     */
    public void setTownCode(String townCode) {
        townCode = townCode.trim();
        StreetFileField type = townCode.matches("\\d+") ? BOE_TOWN_CODE : TOWN_CODE;
        put(type, townCode);
    }

    private static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }
}
