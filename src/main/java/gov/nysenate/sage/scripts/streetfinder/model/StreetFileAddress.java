package gov.nysenate.sage.scripts.streetfinder.model;

import gov.nysenate.sage.util.AddressDictionary;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

/**
 * Represents a Street Address for the StreetFinder database/parsers
 * It also contains helping methods/formatting help for the StreetFinder Parsers
 */
public class StreetFileAddress {
    protected static final String DEFAULT = "\\N";
    private static final Pattern digitPattern = Pattern.compile("-?\\d+(\\.\\d+)?");
    private final StreetFinderBuilding primaryBuilding = new StreetFinderBuilding();
    private String streetSuffix = "";
    protected final Map<StreetFileField, String> fieldMap = new EnumMap<>(StreetFileField.class);

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
        List<String> fieldList = new ArrayList<>();
        for (var fieldType : StreetFileField.values()) {
            if (fieldType == BUILDING) {
                fieldList.addAll(primaryBuilding.getData());
            }
            else {
                fieldList.add(get(fieldType));
            }
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

    public void put(StreetFileField type, String value) {
        if (type == ELECTION_CODE && value.contains("-")) {
            return;
        }
        value = value.replaceAll("\\s+", " ").trim();
        if ((!type.isNonEmpty() || !value.isEmpty()) &&
                (!type.isNumeric() || isNumeric(value))) {
            fieldMap.put(type, value);
        }
    }

    public String get(StreetFileField type) {
        return fieldMap.getOrDefault(type, "\\N");
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
     * Sets the Street Suffix. There's data in {@link AddressDictionary} that we could use to
     * correct this to USPS format, but for now we just correct the address elsewhere.
     */
    public void setStreetSuffix(String streetSuffix) {
        this.streetSuffix = streetSuffix.trim().toUpperCase();
    }

    public String getStreetSuffix() {
        return streetSuffix;
    }

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
        return digitPattern.matcher(strNum).matches();
    }
}
