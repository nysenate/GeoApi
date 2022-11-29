package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;
import gov.nysenate.sage.model.district.DistrictType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import static gov.nysenate.sage.model.address.StreetFileField.*;

/**
 * Parses Montgomery County 2018 txt file
 * Looks for zip, street, low, high, range type, town, ward, district
 * Polling information is unnecessary and skipped
 */
public class MontgomeryParser extends BaseParser {
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
        closeWriters();
    }

    /**
     * Calls all helper methods to parse the line and add data to StreetFinderAddress
     * @param line
     */
    protected void parseLine(String line) {
        StreetFinderAddress streetFinderAddress = new StreetFinderAddress();
        streetFinderAddress.put(ZIP, substringHelper(line, zipIndex, zipIndex + 5));
        getStreetAndSuffix(line, streetFinderAddress);
        getHouseRange(line, streetFinderAddress);
        streetFinderAddress.setBldgParity(getParity(line));
        getTownWardDist(line, streetFinderAddress);
        writeToFile(streetFinderAddress);
    }

    private static String substringHelper(String str, int start, int end) {
        if (start >= str.length()) {
            return "";
        }
        return str.substring(start, end);
    }

    /**
     * Gets the street name and suffix by looking in the locations of StreetNameIndex to HouseRangeIndex
     * Also checks for post or pre directions
     * @param line
     * @param streetFinderAddress
     */
    private void getStreetAndSuffix(String line, StreetFinderAddress streetFinderAddress) {
        line = substringHelper(line, streetNameIndex, houseRangeIndex).trim();
        LinkedList<String> streetSplit = new LinkedList<>(List.of(line.split("\\s+")));

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
        String[] buildingData = substringHelper(line, houseRangeIndex, houseRangeIndex + 12).split("-");
        streetFinderAddress.setBuilding(true, buildingData[0].trim());
        streetFinderAddress.setBuilding(false, buildingData[1].trim());
    }

    private String getParity(String line) {
        return substringHelper(line, houseRangeIndex + 12, houseRangeIndex + 22).trim().split(" ")[0];
    }

    /**
     * Gets the town, ward, and district
     * Or just the town and district if that is all that is there.
     * These are in the format "town/ward/district"
     * @param line
     * @param streetFinderAddress
     */
    private void getTownWardDist(String line, StreetFinderAddress streetFinderAddress) {
        String[] townWardDistrict = substringHelper(line, townWardDistrictIndex, line.length()).trim().split("/");

        if (townWardDistrict.length == 2) {
            streetFinderAddress.put(TOWN, townWardDistrict[0].trim());
            // Skip over word "District"
            String[] district = townWardDistrict[1].trim().split(" ");
            // TODO: I think this is supposed to set the district?
            streetFinderAddress.put(WARD, district[1]);

        } else {
            streetFinderAddress.put(TOWN, townWardDistrict[0].trim());

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
