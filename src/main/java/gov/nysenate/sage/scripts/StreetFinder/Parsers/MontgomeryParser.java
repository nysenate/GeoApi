package gov.nysenate.sage.scripts.StreetFinder.Parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

/**
 * Parses Montgomery County 2018 txt file
 * Looks for zip, street, low, high, range type, town, ward, district
 * Polling information is unnecessary and skipped
 */
public class MontgomeryParser extends NTSParser {
    //indexes keep location of the column of info for each block of data
    private int zipIndex;
    private int StreetNameIndex;
    private int HouseRangeIndex;
    private int TownWardDistrictIndex;
    private String file;

    /**
     * Calls the super constructor which sets up the tsv file
     * @param file
     * @throws IOException
     */
    public MontgomeryParser(String file) throws IOException {
        super(file);
        this.file = file;
    }

    /**
     * Parses the file for the needed information. Finds where the actual data is and then
     * parseLine is called to parse the line
     * @throws FileNotFoundException
     */
    public void parseFile() throws IOException {

        Scanner scanner = new Scanner(new File(file));
        String currentLine = scanner.nextLine();
        boolean inData = false;

        //While there is more lines in the file
        while(scanner.hasNext()) {
            //if inData then check for end of data then call parseLine
            if(inData) {
                //skip over "**TOWN NOT FOUND" And blank lines
                if(!currentLine.trim().isEmpty() && !currentLine.contains("**TOWN NOT FOUND")) {
                    //"r_ppstreet" marks the end of data
                    if(currentLine.contains("r_ppstreet")) {
                        inData = false;
                    } else {
                        parseLine(currentLine);
                    }
                }
                currentLine = scanner.nextLine();
            }
            else {
                //look for "House Range" to signal that data is starting
                if(currentLine.contains("House Range")) {
                    inData = true;
                    //set all indexes
                    zipIndex = currentLine.indexOf("Zip");
                    StreetNameIndex = currentLine.indexOf("Street Name");
                    HouseRangeIndex = currentLine.indexOf("House Range");
                    TownWardDistrictIndex = currentLine.indexOf("Town/Ward/District");
                }
                currentLine = scanner.nextLine();
            }
        }
        //close all writers/readers
        scanner.close();
        super.closeWriters();
    }

    /**
     * Calls all helper methods to parse the line and add data to StreetFinderAddress
     * @param line
     */
    protected void parseLine(String line) {
        StreetFinderAddress StreetFinderAddress = new StreetFinderAddress();

        getZip(line, StreetFinderAddress);
        getStreetAndSuffix(line, StreetFinderAddress);
        getHouseRange(line, StreetFinderAddress);
        getRangeType(line, StreetFinderAddress);
        getTownWardDist(line, StreetFinderAddress);

        super.writeToFile(StreetFinderAddress);
    }

    /**
     * Gets the zip code by looking at the location of zipIndex
     * @param line
     * @param StreetFinderAddress
     */
    private void getZip(String line, StreetFinderAddress StreetFinderAddress) {
        //get zip by going char by char and adding all chars to a string
        StringBuilder zip =  new StringBuilder();
        for(int i = zipIndex; i < zipIndex + 5; i++) {
            zip.append(line.charAt(i));
        }
        StreetFinderAddress.setZip(zip.toString());
    }

    /**
     * Gets the street name and suffix by looking in the locations of StreetNameIndex to HouseRangeIndex
     * Also checks for post or pre directions
     * @param line
     * @param StreetFinderAddress
     */
    private void getStreetAndSuffix(String line, StreetFinderAddress StreetFinderAddress) {
        //get Street name and street suffix char by char
        StringBuilder temp = new StringBuilder();
        for(int i = StreetNameIndex; i < HouseRangeIndex; i++) {
            temp.append(line.charAt(i));
        }
        //split the street name and suffix up by spaces (getting array of words)
        //trim any excess whitespace
        String[] street = temp.toString().trim().split("\\s+");

        int count = 0;
        //check street[0] for pre-Direction
        //special case for "E."
        if(checkForDirection(street[0]) || street[0].equals("E.")) {
            if(street[0].equals("E.")) {
                StreetFinderAddress.setPreDirection("E");
            } else {
                StreetFinderAddress.setPreDirection(street[0]);
            }
            count++;
        }

        temp = new StringBuilder();
        //check for postDirection in the last array location
        if(checkForDirection(street[street.length-1])) {
            //if there is a post-direction then get full Street name word by word
            //assume the second to last word is the suffix
            for(int i = count; i < street.length -2; i++) {
                temp.append(street[i]);
            }
            StreetFinderAddress.setStreet(temp.toString());
            StreetFinderAddress.setStreetSuffix(street[street.length -2]);
            StreetFinderAddress.setPostDirection(street[street.length -1]);
        } else {
            //no postDirection
            //get full Street name word by word
            for(int i = count; i < street.length -1; i++) {
                temp.append(street[i]);
            }
            StreetFinderAddress.setStreet(temp.toString());
            //assume that the last street[] is the street Suffix
            StreetFinderAddress.setStreetSuffix(street[street.length -1]);
        }
    }

    /**
     * Gets the low and high ranges by looking at HouseRangeIndex location
     * @param line
     * @param StreetFinderAddress
     */
    private void getHouseRange(String line, StreetFinderAddress StreetFinderAddress) {
        //get house range
        StringBuilder low = new StringBuilder();
        StringBuilder high = new StringBuilder();
        boolean lowDone = false;
        //go char by char to find low and high seperated by "-"
        for(int i = HouseRangeIndex; i < HouseRangeIndex + 12; i++) {
            if(lowDone) {
                high.append(line.charAt(i));
            } else {
                if(line.charAt(i) == '-') {
                    lowDone = true;
                } else {
                    low.append(line.charAt(i));
                }
            }
        }
        //trim any extra whitespaces
        StreetFinderAddress.setBldg_low(low.toString().trim());
        StreetFinderAddress.setBldg_high(high.toString().trim());
    }

    /**
     * Gets the range type by looking at the location of HouseRangeIndex + 12
     * @param line
     * @param StreetFinderAddress
     */
    private void getRangeType(String line, StreetFinderAddress StreetFinderAddress) {
        //get range type
        //first thing to occur after range (find first character and append until first space
        StringBuilder temp = new StringBuilder();
        boolean found = false;
        //go char by char searching for a non whitespace
        for(int i = HouseRangeIndex + 12; i < HouseRangeIndex + 22; i++) {
            if(!found) {
                //check for non whitespace
                if(!Character.isWhitespace(line.charAt(i))) {
                    temp.append(line.charAt(i));
                    found = true;
                }
            } else {
                //found the range type
                if(Character.isWhitespace(line.charAt(i))) {
                    //exit for loop because range type is fininshed
                    break;
                } else {
                    temp.append(line.charAt(i));
                }
            }
        }

        //check for "EVEN" and change to "EVENS"
        //other range types are in correct format
        if(temp.toString().equals("EVEN")) {
            StreetFinderAddress.setBldg_parity("EVENS");
        } else {
            StreetFinderAddress.setBldg_parity(temp.toString());
        }
    }

    /**
     * Gets the town, ward, and district
     * Or just the town and district if that is all that is there.
     * These are in the format "town/ward/district"
     * @param line
     * @param StreetFinderAddress
     */
    private void getTownWardDist(String line, StreetFinderAddress StreetFinderAddress) {
        //get town/ward/district
        //there might only be a town/district
        //go char by char to end of the line
        StringBuilder temp = new StringBuilder();
        for(int i = TownWardDistrictIndex; i < line.length(); i++) {
            temp.append(line.charAt(i));
        }
        //split into string array by "/"
        String[] TownWardDistrict = temp.toString().trim().split("/");

        //if its only town and district
        if(TownWardDistrict.length == 2) {
            StreetFinderAddress.setTown(TownWardDistrict[0].trim());
            //make into array of string to skip over word "District"
            String[] district = TownWardDistrict[1].trim().split(" ");
            StreetFinderAddress.setWard(district[1]);

        } else {
            //otherwise it must have all 3
            StreetFinderAddress.setTown(TownWardDistrict[0].trim());

            //Make into array of Stirngs to skip over word "Ward
            String[] ward = TownWardDistrict[1].trim().split(" ");
            StreetFinderAddress.setWard(ward[1]);

            //make into array of stirng to skip over word "District"
            String[] district = TownWardDistrict[2].trim().split(" ");
            StreetFinderAddress.setWard(district[1]);
        }
    }
}
