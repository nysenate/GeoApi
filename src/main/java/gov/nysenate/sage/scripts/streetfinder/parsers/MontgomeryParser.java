package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;
import gov.nysenate.sage.model.district.DistrictType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import static gov.nysenate.sage.model.address.StreetFileField.WARD;

/**
 * Parses Montgomery County 2018 txt file
 * Looks for zip, street, low, high, range type, town, ward, district
 * Polling information is unnecessary and skipped
 */
public class MontgomeryParser extends NTSParser {
    // Indices keep location of the column of info for each block of data
    private int zipIndex;
    private int streetNameIndex;
    private int houseRangeIndex;
    private int townWardDistrictIndex;

    /**
     * Calls the super constructor which sets up the tsv file
     * @param file
     * @throws IOException
     */
    public MontgomeryParser(String file) throws IOException {
        super(file);
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

        while (scanner.hasNext()) {
            if (inData && !currentLine.isBlank() && !currentLine.contains("**TOWN NOT FOUND")) {
                //"r_ppstreet" marks the end of data
                if (currentLine.contains("r_ppstreet")) {
                    inData = false;
                } else {
                    parseLine(currentLine);
                }
            }
            else {
                //look for "House Range" to signal that data is starting
                if (currentLine.contains("House Range")) {
                    inData = true;
                    zipIndex = currentLine.indexOf("Zip");
                    streetNameIndex = currentLine.indexOf("Street Name");
                    houseRangeIndex = currentLine.indexOf("House Range");
                    townWardDistrictIndex = currentLine.indexOf("Town/Ward/District");
                }
            }
            currentLine = scanner.nextLine();
        }
        scanner.close();
        super.closeWriters();
    }

    /**
     * Calls all helper methods to parse the line and add data to StreetFinderAddress
     * @param line
     */
    protected void parseLine(String line) {
        StreetFinderAddress streetFinderAddress = new StreetFinderAddress();
        getZip(line, streetFinderAddress);
        getStreetAndSuffix(line, streetFinderAddress);
        getHouseRange(line, streetFinderAddress);
        streetFinderAddress.setBldgParity(getParity(line));
        getTownWardDist(line, streetFinderAddress);
        super.writeToFile(streetFinderAddress);
    }

    /**
     * Gets the zip code by looking at the location of zipIndex
     * @param line
     * @param streetFinderAddress
     */
    private void getZip(String line, StreetFinderAddress streetFinderAddress) {
        //get zip by going char by char and adding all chars to a string
        StringBuilder zip =  new StringBuilder();
        for (int i = zipIndex; i < zipIndex + 5; i++) {
            zip.append(line.charAt(i));
        }
        streetFinderAddress.setZip(zip.toString());
    }

    /**
     * Gets the street name and suffix by looking in the locations of StreetNameIndex to HouseRangeIndex
     * Also checks for post or pre directions
     * @param line
     * @param streetFinderAddress
     */
    private void getStreetAndSuffix(String line, StreetFinderAddress streetFinderAddress) {
        //get Street name and street suffix char by char
        StringBuilder temp = new StringBuilder();
        for (int i = streetNameIndex; i < houseRangeIndex; i++) {
            temp.append(line.charAt(i));
        }
        LinkedList<String> streetSplit = new LinkedList<>(List.of(temp.toString().trim().split("\\s+")));

        // special case for "E."
        String preDir = streetSplit.getFirst().replace(".", "");
        if (checkForDirection(preDir)) {
            streetFinderAddress.setPreDirection(preDir);
            streetSplit.removeFirst();
        }

        if (checkForDirection(streetSplit.getLast())) {
            streetFinderAddress.setPostDirection(streetSplit.removeLast());
        }

        streetFinderAddress.setStreetSuffix(streetSplit.removeLast());
        streetFinderAddress.setStreet(String.join("", streetSplit));
    }

    /**
     * Gets the low and high ranges by looking at HouseRangeIndex location
     * @param line
     * @param streetFinderAddress
     */
    private void getHouseRange(String line, StreetFinderAddress streetFinderAddress) {
        StringBuilder low = new StringBuilder();
        StringBuilder high = new StringBuilder();
        boolean lowDone = false;
        for (int i = houseRangeIndex; i < houseRangeIndex + 12; i++) {
            if (lowDone) {
                high.append(line.charAt(i));
            } else {
                if (line.charAt(i) == '-') {
                    lowDone = true;
                } else {
                    low.append(line.charAt(i));
                }
            }
        }
        streetFinderAddress.setBuilding(true, low.toString().trim());
        streetFinderAddress.setBuilding(false, high.toString().trim());
    }

    private String getParity(String line) {
        //get range type
        //first thing to occur after range (find first character and append until first space
        StringBuilder temp = new StringBuilder();
        boolean found = false;
        //go char by char searching for a non whitespace
        for (int i = houseRangeIndex + 12; i < houseRangeIndex + 22; i++) {
            if (!found) {
                //check for non whitespace
                if (!Character.isWhitespace(line.charAt(i))) {
                    temp.append(line.charAt(i));
                    found = true;
                }
            } else {
                if (Character.isWhitespace(line.charAt(i))) {
                    break;
                } else {
                    temp.append(line.charAt(i));
                }
            }
        }

        if (temp.toString().equals("EVEN")) {
            temp.append("S");
        }
        return temp.toString();
    }

    /**
     * Gets the town, ward, and district
     * Or just the town and district if that is all that is there.
     * These are in the format "town/ward/district"
     * @param line
     * @param streetFinderAddress
     */
    private void getTownWardDist(String line, StreetFinderAddress streetFinderAddress) {
        StringBuilder temp = new StringBuilder();
        for (int i = townWardDistrictIndex; i < line.length(); i++) {
            temp.append(line.charAt(i));
        }
        String[] townWardDistrict = temp.toString().trim().split("/");

        if (townWardDistrict.length == 2) {
            streetFinderAddress.setTown(townWardDistrict[0].trim());
            // Skip over word "District"
            String[] district = townWardDistrict[1].trim().split(" ");
            // TODO: I think this is supposed to set the district?
            streetFinderAddress.put(WARD, district[1]);

        } else {
            streetFinderAddress.setTown(townWardDistrict[0].trim());

            // Skip over word "Ward"
            String[] ward = townWardDistrict[1].trim().split(" ");
            streetFinderAddress.put(WARD, ward[1]);

            // Skip over word "District"
            String[] district = townWardDistrict[2].trim().split(" ");
            // TODO: this as well?
            streetFinderAddress.put(WARD, district[1]);
        }
    }
}
