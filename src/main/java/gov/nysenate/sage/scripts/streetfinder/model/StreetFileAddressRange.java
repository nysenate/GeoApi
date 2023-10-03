package gov.nysenate.sage.scripts.streetfinder.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

/**
 * Represents a street address, as parsed from a streetfile.
 */
public class StreetFileAddressRange {
    protected static final String DEFAULT = "\\N";
    private static final Pattern digitPattern = Pattern.compile("-?\\d+(\\.\\d+)?");
    private final BuildingRange primaryBuilding = new BuildingRange();
    protected final Map<StreetFileField, String> fieldMap = new EnumMap<>(StreetFileField.class);

    public StreetFileAddressRange() {
        put(STATE, "NY");
    }

    public StreetFileAddressRange(StreetFileAddressRange toCopy) {
        this.fieldMap.putAll(toCopy.fieldMap);
    }

    /**
     * Converts the StreetFinderAddress Object into a String in the format needed for the SQL file
     * @return Object in String form
     */
    public String toStreetFileForm() {
        var fieldList = new ArrayList<>();
        for (var fieldType : StreetFileField.values()) {
            if (fieldType == BUILDING) {
                fieldList.addAll(primaryBuilding.getData());
            }
            else {
                fieldList.add(get(fieldType));
            }
        }
        return fieldList.stream().map(field -> field == null ? DEFAULT : field.toString())
                .collect(Collectors.joining("\t"));
    }

    public void addToStreet(String toAdd) {
        if (!fieldMap.containsKey(STREET)) {
            put(STREET, "");
        }
        put(STREET, (get(STREET) + " " + toAdd).trim().replaceAll("\\s+", " "));
    }

    public String get(StreetFileField type) {
        return fieldMap.get(type);
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

    public BuildingRange getBuildingRange() {
        return primaryBuilding;
    }

    public void setBuilding(boolean isLow, String data) {
        primaryBuilding.setData(isLow, data);
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
