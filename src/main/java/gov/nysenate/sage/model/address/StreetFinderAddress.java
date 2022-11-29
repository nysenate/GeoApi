package gov.nysenate.sage.model.address;

import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.util.AddressDictionary;
import org.apache.logging.log4j.util.Strings;

import java.util.*;
import java.util.regex.Pattern;

import static gov.nysenate.sage.model.district.DistrictType.*;

/**
 * Represents a Street Address for the StreetFinder database/parsers
 * It also contains helping methods/formatting help for the StreetFinder Parsers
 */
public class StreetFinderAddress {
    private static final Set<DistrictType> mustNotBeEmpty = EnumSet.of(TOWN, WARD, CONGRESSIONAL, SENATE, ASSEMBLY, CLEG, VILLAGE, FIRE, CITY_COUNCIL);
    private static final Set<DistrictType> mustBeNumeric = EnumSet.of(CONGRESSIONAL, SENATE, ASSEMBLY, CITY_COUNCIL);
    private static final Set<DistrictType> defaultTypes = EnumSet.allOf(DistrictType.class);
    static {
        defaultTypes.remove(COUNTY);
        defaultTypes.remove(ELECTION);
    }
    private final StreetFinderBuilding primaryBuilding = new StreetFinderBuilding();
    // Really a range for apartment buildings. Only used in Suffolk.
    protected final StreetFinderBuilding secondaryBuilding = new StreetFinderBuilding();
    private String preDirection;
    private String postDirection;
    private String street;
    private String streetSuffix;
    private String townCode;
    // TODO: might be the county?
    private String dist;
    private final Map<DistrictType, String> districtTypeMap = new EnumMap<>(DistrictType.class);
    private String ed;
    private String boeTownCode;
    private String boeSchool;
    private static final Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    /**
     * Default Constructor that sets all fields (except for pre-Direction, post-Direction, and Street Suffix because those are appended to the Street Name)
     * to \N which the default for blank in the geoapi database
     */
    public StreetFinderAddress() {
        preDirection = "";
        street = "\\N";
        streetSuffix = "";
        townCode = "\\N";
        dist = "\\N";
        ed = "\\N";
        boeTownCode = "\\N";
        boeSchool = "\\N";
    }

    /**
     * Converts the StreetFinderAddress Object into a String in the format needed for the SQL file
     * @return Object in String form
     */
    public String toStreetFileForm() {
        List<String> start = List.of(preDirection + street + " " + streetSuffix, get(TOWN), "NY", get(ZIP));
        List<String> higherDistricts = List.of(get(ASSEMBLY), get(SENATE), get(CONGRESSIONAL));
        List<String> middleData = List.of(boeTownCode, townCode, get(WARD), boeSchool);
        List<String> lowerDistricts = List.of(get(SCHOOL), get(CLEG), get(CITY_COUNCIL), get(FIRE), get(CITY), get(VILLAGE));
        List<String> data = new ArrayList<>(start);
        data.addAll(primaryBuilding.getData());
        data.addAll(secondaryBuilding.getData());
        data.add(ed);
        data.add("\\N");
        data.addAll(higherDistricts);
        data.addAll(middleData);
        data.addAll(lowerDistricts);
        return String.join("\t", data) + "\n";
    }

    public void put(DistrictType type, String value) {
        if ((!mustNotBeEmpty.contains(type) || !value.isEmpty()) &&
                (!mustBeNumeric.contains(type) || isNumeric(value))) {
            districtTypeMap.put(type, value);
        }
    }

    public boolean hasSenateDistrict() {
        return districtTypeMap.containsKey(SENATE);
    }

    private String get(DistrictType type) {
        return districtTypeMap.getOrDefault(type, "\\N");
    }

    public static StreetFinderAddress normalize(StreetFinderAddress streetFinderAddress) {
        String prefix = streetFinderAddress.getPreDirection().trim().toUpperCase();
        String street = streetFinderAddress.getStreet().trim().toUpperCase();
        String suffix = streetFinderAddress.getStreetSuffix().trim().toUpperCase();
        String location = streetFinderAddress.get(TOWN).trim().toUpperCase();

        streetFinderAddress.setPreDirection(prefix);
        streetFinderAddress.setStreet(street);
        streetFinderAddress.setStreetSuffix(suffix);
        streetFinderAddress.setTown(location);

        return streetFinderAddress;
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
     * Accessor method
     * @return
     */
    public String getPreDirection() {
        return preDirection;
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
        //Check for matches in AddressDictionary highWayMap
        for (String s : string) {
            tempStreet += s;
            //if tempStreet is found as a key then set tempStreet to the keys value
            if (AddressDictionary.highWayMap.containsKey(tempStreet)) {
                tempStreet = AddressDictionary.highWayMap.get(tempStreet).toUpperCase();
            }
            tempStreet += " ";
        }
        this.street = tempStreet.trim();
    }

    /**
     * Accessor Method
     * @return
     */
    public String getStreet() {
        return street;
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

    public boolean hasBuildingParity() {
        return primaryBuilding.hasParity();
    }

    /**
     * Sets the 5-digit zipcode
     * @param zip
     */
    public void setZip(String zip) {
        put(ZIP, zip);
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
     * Sets the town name
     * @param town
     */
    public void setTown(String town) {
        put(TOWN, town.trim());
    }

    /**
     * Sets the townCode. Checks if the code is numbers or if the code is characters.
     * If it is numbers then it is passes to setBoeTownCode
     * @param townCode - Can either be ints or a combination of characters and numbers
     */
    public void setTownCode(String townCode) {
        if(!townCode.isEmpty()) {
            if (townCode.matches("\\d+")) {
                this.setBoeTownCode(townCode.trim());
            } else {
                this.townCode = townCode.trim();
            }
        }
    }

    /**
     * Sets the district
     * @param dist
     */
    public void setDist(String dist) {
        if (!dist.isEmpty()) {
            this.dist = dist;
        }
    }


    /**
     * Sets the School_Code. Checks for a School_code of number or a school_code of all characters
     * If it is all character then it is a Boe_School_Code
     * @param sch - Can either be ints or characters
     */
    public void setSch(String sch) {
        if (sch.matches("\\d+")) {
            put(SCHOOL, sch);
        } else if(sch.matches("\\S+")) {
            this.boeSchool = sch;
        }
    }

    /**
     * Sets the election district
     * @param ed
     */
    public void setED(String ed) {
        if(!ed.isEmpty() && isNumeric(ed) && !ed.contains("-")) {
            this.ed = ed;
        }
    }

    /**
     * Sets the boeTownCode. Is only called by setTownCode if the code is digits
     * @param boeTownCode
     */
    private void setBoeTownCode(String boeTownCode) {
        this.boeTownCode = boeTownCode;
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }
}
