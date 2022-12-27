package gov.nysenate.sage.scripts.streetfinder.model;

import gov.nysenate.sage.util.AddressDictionary;

import java.util.*;
import java.util.regex.Pattern;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

/**
 * Represents a Street Address for the StreetFinder database/parsers
 * It also contains helping methods/formatting help for the StreetFinder Parsers
 */
public class StreetFileAddress {
    protected static final String DEFAULT = "\\N";
    private static final Set<StreetFileField> mustNotBeEmpty = EnumSet.of(ELECTION_CODE, WARD,
            CONGRESSIONAL, SENATE, ASSEMBLY, CLEG, VILLAGE, FIRE, CITY_COUNCIL, SENATE_TOWN_ABBREV, BOE_TOWN_CODE, COUNTY_ID);
    private static final Set<StreetFileField> mustBeNumeric = EnumSet.of(ELECTION_CODE, CONGRESSIONAL, SENATE, ASSEMBLY, CITY_COUNCIL);
    private final StreetFinderBuilding primaryBuilding = new StreetFinderBuilding();
    // Really a range for apartment buildings. Only used in Suffolk.
    protected final StreetFinderBuilding secondaryBuilding = new StreetFinderBuilding();
    // TODO: may not be used
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
        // This will be set by CountyParserMatcher
        fieldMap.remove(COUNTY_ID);
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
        return String.join("\t", fieldList);
    }

    public String getLowString() {
        return primaryBuilding.getLowString();
    }

    public void addToStreet(String toAdd) {
        if (get(STREET).equals(DEFAULT)) {
            put(STREET, "");
        }
        put(STREET, (get(STREET) + " " + toAdd).trim().replaceAll("\\s+", " "));
    }

    public String getZip() {
        return get(ZIP);
    }

    public void put(StreetFileField type, String value) {
        if (type == ELECTION_CODE && value.contains("-")) {
            return;
        }
        value = value.replaceAll("\\s+", " ");
        if ((!mustNotBeEmpty.contains(type) || !value.isEmpty()) &&
                (!mustBeNumeric.contains(type) || isNumeric(value))) {
            fieldMap.put(type, value);
        }
    }

    public String get(StreetFileField type) {
        return fieldMap.getOrDefault(type, "\\N");
    }

    public void normalize() {
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
        StreetFileField type = townCode.matches("\\d+") ? BOE_TOWN_CODE : SENATE_TOWN_ABBREV;
        put(type, townCode);
    }

    private static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }
}
