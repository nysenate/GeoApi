package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.district.County;
import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;
import gov.nysenate.sage.scripts.streetfinder.model.BuildingRange;
import gov.nysenate.sage.scripts.streetfinder.model.StreetfileAddressRange;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import static gov.nysenate.sage.scripts.streetfinder.parsers.NTSParser.substringHelper;

/**
 * Parses Montgomery County 2018 txt file
 * Looks for zip, street, low, high, range type, town, ward, district
 * Polling information is unnecessary and skipped
 */
public class MontgomeryParser extends CountyParser {
    // Indices keep location of the column of info for each block of data
    private int zipIndex;
    private int streetNameIndex;
    private int houseRangeIndex;
    private int townWardDistrictIndex;

    /**
     * Calls the super constructor which sets up the tsv file
     * @param file
     */
    public MontgomeryParser(File file, County county) {
        super(file, county);
    }

    /**
     * Parses the file for the needed information. Finds where the actual data is and then
     * parseLine is called to parse the line
     * @throws FileNotFoundException
     */
    public void parseFile() throws IOException {
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
    }

    @Override
    protected StreetfileDataExtractor getDataExtractor() {
        return null;
    }

    /**
     * Calls all helper methods to parse the line and add data to StreetFinderAddress
     * TODO: should parse on whitespace, not indices
     */
    @Override
    protected String[] parseLine(String line) {
        String zip5 = substringHelper(line, zipIndex, zipIndex + 5);
        String street = getStreetAndSuffix(line);
        String[] bldgData = substringHelper(line, houseRangeIndex, houseRangeIndex + 12).split("-");
        var buildingRange = new BuildingRange(bldgData[0], bldgData[1], getParity(line));
        var range = new StreetfileAddressRange(buildingRange, new AddressWithoutNum(street, "", zip5));
        getTownWardDist(line, range);
        // TODO
        return null;
    }

    /**
     * Gets the street name and suffix by looking in the locations of StreetNameIndex to HouseRangeIndex
     * Also checks for post or pre directions
     * @param line
     */
    private String getStreetAndSuffix(String line) {
        line = substringHelper(line, streetNameIndex, houseRangeIndex).trim();
        LinkedList<String> streetSplit = new LinkedList<>(List.of(line.split("\\s+")));
        return String.join(" ", streetSplit);
    }

    private String getParity(String line) {
        return substringHelper(line, houseRangeIndex + 12, houseRangeIndex + 22).trim().split(" ")[0];
    }

    /**
     * Gets the town, ward, and district
     * Or just the town and district if that is all that is there.
     * These are in the format "town/ward/district"
     * @param line
     * @param range
     */
    private void getTownWardDist(String line, StreetfileAddressRange range) {
        String[] townWardDistrict = substringHelper(line, townWardDistrictIndex, line.length()).trim().split("/");
        // TODO
    }
}
