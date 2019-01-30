package gov.nysenate.sage.model.address;

import gov.nysenate.sage.util.AddressDictionary;
import gov.nysenate.sage.util.StreetAddressParser;

/**
 * Represents a Street Address for the StreetFinder database/parsers
 * It also contains helping methods/formatting help for the StreetFinder Parsers
 */
public class StreetFinderAddress {

    //Fields
    private String preDirection;
    private String postDirection;
    private String street;
    private String streetSuffix;
    private String zip;
    private String bldg_low;
    private String bldg_low_char;
    private String bldg_high;
    private String bldg_high_char;
    private String bldg_parity;
    private String town;
    private String townCode;
    private String ward;
    private String dist;
    private String cong;
    private String sen;
    private String asm;
    private String sch;
    private String cle;
    private String vill;
    private String fire;
    private String secondaryBldg_low;
    private String secondaryBldg_low_char;
    private String secondaryBldg_high;
    private String secondaryBldg_high_char;
    private String secondaryBldg_parity;
    private String cc;
    private String ed;
    private String boeTownCode;
    private String boeSchool;
    private String cityCode;
    //used as helper storage
    private String digits, characters;

    /**
     * Default Constructor that sets all fields (except for pre-Direction, post-Direction, and Street Suffix because those are appended to the Street Name)
     * to \N which the default for blank in the geoapi database
     */
    public StreetFinderAddress() {
        preDirection = "";
        street = "\\N";
        streetSuffix = "";
        zip = "\\N";
        bldg_low = "\\N";
        bldg_low_char = "\\N";
        bldg_high = "\\N";
        bldg_high_char = "\\N";
        bldg_parity = "\\N";
        town = "\\N";
        townCode = "\\N";
        ward = "\\N";
        dist = "\\N";
        cong = "\\N";
        sen = "\\N";
        asm = "\\N";
        sch = "\\N";
        cle = "\\N";
        vill = "\\N";
        fire = "\\N";
        cc = "\\N";
        ed = "\\N";
        boeTownCode = "\\N";
        boeSchool = "\\N";
        cityCode = "\\N";
        secondaryBldg_low = "\\N";
        secondaryBldg_low_char = "\\N";
        secondaryBldg_high = "\\N";
        secondaryBldg_high_char = "\\N";
        secondaryBldg_parity = "\\N";
    }

    /**
     * Converts the StreetFinderAddress Object into a String in the format needed for the SQL file
     * @return Object in String form
     */
    public String toStreetFileForm() {
        return preDirection + street + " " + streetSuffix + "\t" + town + "\t" + "NY" + "\t" + zip + "\t" + bldg_low + "\t" + bldg_low_char + "\t" + bldg_high +
                "\t" + bldg_high_char + "\t" + bldg_parity + "\t" + secondaryBldg_low + "\t" + secondaryBldg_low_char + "\t" + secondaryBldg_high + "\t" + secondaryBldg_high_char + "\t" + secondaryBldg_parity + "\t" +
                ed + "\t" + "\\N" + "\t" + asm + "\t" + sen + "\t" + cong + "\t" + boeTownCode + "\t" + townCode + "\t" +
                ward + "\t" + boeSchool + "\t" + sch + "\t" + cle + "\t" + cc + "\t" + fire + "\t" + cityCode + "\t" + vill +"\n";
    }

    public static StreetFinderAddress normalize(StreetFinderAddress streetFinderAddress) {
        String prefix = streetFinderAddress.getPreDirection().trim().toUpperCase();
        String street = streetFinderAddress.getStreet().trim().toUpperCase();
        String suffix = streetFinderAddress.getStreetSuffix().trim().toUpperCase();
        String location = streetFinderAddress.getTown().trim().toUpperCase();

        streetFinderAddress.setPreDirection(prefix);
        streetFinderAddress.setStreet(street);
        streetFinderAddress.setStreetSuffix(suffix);
        streetFinderAddress.setTown(location);

        return streetFinderAddress;
    }

    /**
     * Sets the pre-Direction. Should be in the standard form of N, E, S, W
     * @param preDirection
     */
    public void setPreDirection(String preDirection) {
        //Check for a blank argument
        if(preDirection.trim().length() > 0 ) {
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
        if(postDirection != null)
            this.postDirection = postDirection;
    }

    /**
     * Sets the Street Name. Checks the Street Name for possible conversions into the format
     * Used by USPS
     * @param street
     */
    public void setStreet(String street) {
        if(street != null && !street.equals("")) {
            String tempStreet = "";
            String[] string = street.split("\\s+");
            //Check for matches in AddressDictionary highWayMap
            for(int i = 0; i < string.length; i++) {
                tempStreet = tempStreet + string[i];
                //if tempStreet is found as a key then set tempStreet to the keys value
                if(AddressDictionary.highWayMap.containsKey(tempStreet)) {
                    tempStreet = AddressDictionary.highWayMap.get(tempStreet).toUpperCase();
                }
                tempStreet += " ";
            }
            //trim off extra whitespace
            this.street = tempStreet.trim();
        }
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
        if(streetSuffix != null) {
            //Trim off possible . from suffix ex. "Ext."
            String string = streetSuffix.replace(".", "");
            //First check the streetTypeMap
            if(AddressDictionary.streetTypeMap.containsKey(string.toUpperCase())) {
                this.streetSuffix = AddressDictionary.streetTypeMap.get(string.toUpperCase()).toUpperCase();
                //Then check highWayMap
            } else if(AddressDictionary.highWayMap.containsKey(string.toUpperCase())) {
                this.streetSuffix = AddressDictionary.highWayMap.get(string.toUpperCase()).toUpperCase();
            } else {
                //If not found in either hashMap then just set to the regular string
                this.streetSuffix = string.toUpperCase();
            }
        }
    }

    /**
     * Accessor method
     * @return
     */
    public String getStreetSuffix() {
        return streetSuffix;
    }

    public String getSenateDistrict() { return sen; }

    public String getBldg_parity() {
        return bldg_parity;
    }

    /**
     * Sets the 5-digit zipcode
     * @param zip
     */
    public void setZip(String zip) {
        if(!zip.equals("")) {
            this.zip = zip;
        }
    }

    /**
     * Sets the low building number of the street. Method checks for letters in the building number and sets the building
     * low character if any characters are found.
     * @param bldg_low - Can either be all ints or a combination of characters and ints
     */
    public void setBldg_low(String bldg_low) {
        if(bldg_low != null && !bldg_low.equals("")) {
            //set to a char array to cycle through to look for characters
            char[] temp = bldg_low.toCharArray();
            digits = "";
            characters = "";
            this.setDigitsAndCharacters(temp);
            //Check to make sure that there were digits
            if(!digits.equals(""))
                this.bldg_low = digits;
            //set the building low character. A check for empty is within that method
            this.setBldg_low_char(characters);
        }
    }

    /**
     * Sets the high building number of the street. Method checks for letters in the building number and sets the building
     * high character if any characters are found.
     * @param bldg_high - Can either be all ints or a combination of characters and ints
     */
    public void setBldg_high(String bldg_high) {
        if(bldg_high != null&& !bldg_high.equals("")) {
            //set to a char array to cycle through to look for characters
            char[] temp = bldg_high.toCharArray();
            digits = "";
            characters = "";
            this.setDigitsAndCharacters(temp);
            //Check to make sure that there were digits
            if(!digits.equals(""))
                this.bldg_high = digits;
            //set the building low character. A check for empty is within that method
            this.setBldg_high_char(characters);
        }
    }

    /**
     * Sets the building Parity. It is assumed to be in the form of "ODDS", "EVENS", or "ALL"
     * @param bldg_parity
     */
    public void setBldg_parity(String bldg_parity) {
        if(!bldg_parity.equals("")) {
            this.bldg_parity = bldg_parity;
        }
    }

    /**
     * Sets the town name
     * @param town
     */
    public void setTown(String town) {
        if(!town.equals("")) {
            //trim any possible extra whitespace
            this.town = town.trim();
        }
    }

    /**
     * Sets the townCode. Checks if the code is numbers or if the code is characters.
     * If it is numbers then it is passes to setBoeTownCode
     * @param townCode - Can either be ints or a combination of characters and numbers
     */
    public void setTownCode(String townCode) {
        if(!townCode.equals("")) {
            //check to see it it matches all numbers
            if(townCode.matches("\\d+")) {
                //All numbers so it is a Boe Town Code
                this.setBoeTownCode(townCode.trim());
            } else {
                //otherwise it is a townCode
                this.townCode = townCode.trim();
            }
        }
    }

    /**
     * Sets the wardCode
     * @param ward
     */
    public void setWard(String ward) {
        if(!ward.equals("")) {
            this.ward = ward;
        }
    }

    /**
     * Sets the district
     * @param dist
     */
    public void setDist(String dist) {
        if(!dist.equals("")) {
            this.dist = dist;
        }
    }

    /**
     * Sets the congressional_code
     * @param cong
     */
    public void setCong(String cong) {
        if(!cong.equals("")) {
            this.cong = cong;
        }
    }

    /**
     * Sets the senate_code
     * @param sen
     */
    public void setSen(String sen) {
        if(!sen.equals("")) {
            this.sen = sen;
        }
    }

    /**
     * Sets the assembly_Code
     * @param asm
     */
    public void setAsm(String asm) {
        if(!asm.equals("")) {
            this.asm = asm;
        }
    }

    /**
     * Accessor Method
     * @return
     */
    public String getAsm() {
        return asm;
    }


    /**
     * Sets the School_Code. Checks for a School_code of number or a school_code of all characters
     * If it is all character then it is a Boe_School_Code
     * @param sch - Can either be ints or characters
     */
    public void setSch(String sch) {
        //check for all numbers
        if(sch.matches("\\d+")) {
            this.sch = sch;
            //check for all characters
        } else if(sch.matches("\\S+")) {
            //all characters. call setBoeSchool
            this.setBoeSchool(sch);
        }
    }

    /**
     * Sets the Cleg_Code
     * @param cle
     */
    public void setCle(String cle) {
        if(!cle.equals("")) {
            this.cle = cle;
        }
    }

    /**
     * Sets the village code
     * @param vill
     */
    public void setVill(String vill) {
        if(!vill.equals("")) {
            this.vill = vill;
        }
    }

    /**
     * Sets the fire_code
     * @param fire
     */
    public void setFire(String fire) {
        if(!fire.equals("")) {
            this.fire = fire;
        }
    }

    /**
     * Sets the cc_code
     * @param cc
     */
    public void setCC(String cc) {
        if(!cc.equals("")) {
            this.cc = cc;
        }
    }

    /**
     * Sets the election district
     * @param ed
     */
    public void setED(String ed) {
        if(!ed.equals("")) {
            this.ed = ed;
        }
    }

    /**
     * Sets the apt_bldg_parity. Assumes the format or "ODDS", "EVENS", or "ALL"
     * @param secondaryBldg_parity
     */
    public void setSecondaryBldg_parity(String secondaryBldg_parity) {
        if(!secondaryBldg_parity.equals("")) {
            this.secondaryBldg_parity = secondaryBldg_parity;
        }
    }

    /**
     * Sets the apt_bldg_lo_num. Also checks for any characters in the numbers. If
     * Any characters are found then setSecondaryBldg_low_char is called
     * @param secondaryBldg_low - Can either be ints or a combination of characters and numbers
     */
    public void setSecondaryBldg_low(String secondaryBldg_low) {
        if(secondaryBldg_low != null && !secondaryBldg_low.equals("")) {
            //split into char array to look for characters
            char[] temp = secondaryBldg_low.toCharArray();
            digits = "";
            characters = "";
            this.setDigitsAndCharacters(temp);
            //check for an empty digits
            if(!digits.equals(""))
                this.secondaryBldg_low = digits;
            //call bldg_low_char. Empty check is handle within the method
            this.setSecondaryBldg_low_char(characters);
        }
    }

    /**
     * Sets the apt_bldg_high_num. Also checks for any characters in the numbers. If
     *  Any characters are found then setSecondaryBldg_high_char is called
     * @param secondaryBldg_high - Can either be ints or a combination of characters and numbers
     */
    public void setSecondaryBldg_high(String secondaryBldg_high) {
        if(secondaryBldg_high != null && !secondaryBldg_high.equals("")) {
            //split into char array to look for characters
            char[] temp = secondaryBldg_low.toCharArray();
            //if it is a number
            digits = "";
            characters = "";
            this.setDigitsAndCharacters(temp);
            //check for an empty digits
            if(!digits.equals(""))
                this.secondaryBldg_high = digits;
            //call bldg_high_char. Empty check is handle within the method
            this.setSecondaryBldg_high_char(characters);
        }
    }

    /**
     * Sets the bldg_low_char
     * @param bldg_low_char
     */
    public void setBldg_low_char(String bldg_low_char) {
        if(!bldg_low_char.equals(""))
            this.bldg_low_char = bldg_low_char;
    }

    /**
     * Sets the bldg_high_char
     * @param bldg_high_char
     */
    public void setBldg_high_char(String bldg_high_char) {
        if(!bldg_high_char.equals(""))
            this.bldg_high_char = bldg_high_char;
    }

    /**
     * Sets the secondary_bldg_low_char
     * @param secondaryBldg_low_char
     */
    public void setSecondaryBldg_low_char(String secondaryBldg_low_char) {
        if(!secondaryBldg_low_char.equals(""))
            this.secondaryBldg_low_char = secondaryBldg_low_char;
    }

    /**
     * Sets the secondary bldg_high_char
     * @param secondaryBldg_high_char
     */
    public void setSecondaryBldg_high_char(String secondaryBldg_high_char) {
        if(!secondaryBldg_high_char.equals(""))
            this.secondaryBldg_high_char = secondaryBldg_high_char;
    }

    /**
     * Sets the boeTownCode. Is only called by setTownCode if the code is digits
     * @param boeTownCode
     */
    private void setBoeTownCode(String boeTownCode) {
        this.boeTownCode = boeTownCode;
    }

    /**
     * Sets the boeSchoolCode. Is only called by setSch if the code is characters
     * @param boeSchool
     */
    private void setBoeSchool(String boeSchool) {
        this.boeSchool = boeSchool;
    }

    /**
     * Sets the city Code
     * @param cityCode
     */
    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    /**
     * Sets the digits string and characters string with the corresponding
     * data in temp. This is used for setting bldg_low, bldg_high, secondaryBldg_low, secondaryBldg_high
     * and filtering out any characters into the respective Bldg_high_char fields
     * @param temp
     */
    private void setDigitsAndCharacters(char[] temp) {
        for(int i = 0; i < temp.length; i++) {
            //if its a digit then added to digits String
            if(Character.isDigit(temp[i])) {
                digits += temp[i];
                //if its a character then add to character String
            } else if(Character.isLetter(temp[i])) {
                characters += temp[i];
            }
        }
    }


    public String getPostDirection() {
        return postDirection;
    }

    public String getZip() {
        return zip;
    }

    public String getBldg_low() {
        return bldg_low;
    }

    public String getTown() {
        return town;
    }
}
