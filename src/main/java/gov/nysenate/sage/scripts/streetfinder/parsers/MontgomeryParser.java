package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileFunctionList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;
import static gov.nysenate.sage.scripts.streetfinder.parsers.NTSParser.substringHelper;

/**
 * Parses Montgomery County 2018 txt file
 * Looks for zip, street, low, high, range type, town, ward, district
 * Polling information is unnecessary and skipped
 */
public class MontgomeryParser extends BasicParser {
    // Indices keep location of the column of info for each block of data
    private int zipIndex;
    private int streetNameIndex;
    private int houseRangeIndex;
    private int townWardDistrictIndex;

    /**
     * Calls the super constructor which sets up the tsv file
     * @param file
     */
    public MontgomeryParser(File file) {
        super(file);
    }

    /**
     * Parses the file for the needed information. Finds where the actual data is and then
     * parseLine is called to parse the line
     * @throws FileNotFoundException
     */
    public List<StreetFileAddress> parseFile() throws IOException {
        Scanner scanner = new Scanner(file);
        String currentLine = scanner.nextLine();
        boolean inData = false;

        while (scanner.hasNextLine()) {
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
        endProcessing();
        return null;
    }

    @Override
    protected StreetFileFunctionList<StreetFileAddress> getFunctions() {
        return null;
    }

    /**
     * Calls all helper methods to parse the line and add data to StreetFinderAddress
     * TODO: should parse on whitespace, not indices
     */
    protected void parseLine(String line) {
        StreetFileAddress streetFileAddress = new StreetFileAddress();
        streetFileAddress.put(ZIP, substringHelper(line, zipIndex, zipIndex + 5));
        getStreetAndSuffix(line, streetFileAddress);
        getHouseRange(line, streetFileAddress);
        streetFileAddress.setBldgParity(getParity(line));
        getTownWardDist(line, streetFileAddress);
        finalize(streetFileAddress);
    }

    /**
     * Gets the street name and suffix by looking in the locations of StreetNameIndex to HouseRangeIndex
     * Also checks for post or pre directions
     * @param line
     * @param streetFileAddress
     */
    private void getStreetAndSuffix(String line, StreetFileAddress streetFileAddress) {
        line = substringHelper(line, streetNameIndex, houseRangeIndex).trim();
        LinkedList<String> streetSplit = new LinkedList<>(List.of(line.split("\\s+")));
        streetFileAddress.put(STREET, String.join(" ", streetSplit));
    }

    /**
     * Gets the low and high ranges by looking at HouseRangeIndex location
     * @param line
     * @param streetFileAddress
     */
    private void getHouseRange(String line, StreetFileAddress streetFileAddress) {
        String[] buildingData = substringHelper(line, houseRangeIndex, houseRangeIndex + 12).split("-");
        streetFileAddress.setBuilding(true, buildingData[0].trim());
        streetFileAddress.setBuilding(false, buildingData[1].trim());
    }

    private String getParity(String line) {
        return substringHelper(line, houseRangeIndex + 12, houseRangeIndex + 22).trim().split(" ")[0];
    }

    /**
     * Gets the town, ward, and district
     * Or just the town and district if that is all that is there.
     * These are in the format "town/ward/district"
     * @param line
     * @param streetFileAddress
     */
    private void getTownWardDist(String line, StreetFileAddress streetFileAddress) {
        String[] townWardDistrict = substringHelper(line, townWardDistrictIndex, line.length()).trim().split("/");

        streetFileAddress.put(TOWN, townWardDistrict[0].trim());
        if (townWardDistrict.length == 2) {
            // Skip over word "District"
            String[] district = townWardDistrict[1].trim().split(" ");
            // TODO: I think this is supposed to set the district?
            streetFileAddress.put(WARD, district[1]);

        } else {
            // Skip over word "Ward"
            String[] ward = townWardDistrict[1].trim().split(" ");
            streetFileAddress.put(WARD, ward[1]);

            // Skip over word "District"
            String[] district = townWardDistrict[2].trim().split(" ");
            // TODO: this as well?
            streetFileAddress.put(WARD, district[1]);
        }
    }
}
