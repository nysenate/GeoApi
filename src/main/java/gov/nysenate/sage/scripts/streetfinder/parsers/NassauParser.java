package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;

import java.io.IOException;

import static gov.nysenate.sage.model.address.StreetFileField.*;

/**
 * Parses Nassau County csv file and outputs a tsv file
 */
public class NassauParser extends NTSParser {
    /**
     * Calls the super constructor which sets up the tsv output file
     * @param file
     * @throws IOException
     */
    public NassauParser(String file) throws IOException {
        super(file);
    }

    @Override
    public void parseFile() throws IOException {
        readFile();
    }

    /**
     * Parses the line by calling each helper method to extract all data
     * @param line
     */
    @Override
    protected void parseLine(String line) {
        StreetFinderAddress streetFinderAddress = new StreetFinderAddress();
        String[] splitLine = line.split(",");

        handlePrecinct(splitLine[0], streetFinderAddress);
        streetFinderAddress.setStreet(splitLine[1]);
        getSuffix(splitLine[2], streetFinderAddress);
        streetFinderAddress.setTown(splitLine[3]);
        streetFinderAddress.setZip(splitLine[4]);
        streetFinderAddress.setBuilding(true, splitLine[5]);
        streetFinderAddress.setBuilding(false, splitLine[6]);
        streetFinderAddress.setBldgParity(splitLine[7]);
        // TODO: No use of the 8th part?
        streetFinderAddress.put(CONGRESSIONAL, trim(splitLine[9]));
        streetFinderAddress.put(SENATE, trim(splitLine[10]));
        streetFinderAddress.put(ASSEMBLY, trim(splitLine[11]));
        streetFinderAddress.put(CLEG, trim(splitLine[12]));
        // Ignore TD
        writeToFile(streetFinderAddress);
    }

    private static void getSuffix(String data, StreetFinderAddress streetFinderAddress) {
        String streetSuffix = data.replace(streetFinderAddress.getStreet(), " ").trim();
        String[] splitString = streetSuffix.split("\\s+");
        int suffixIndex = 0;
        if (splitString.length > 1) {
            if (checkForDirection(splitString[1])) {
                streetFinderAddress.setPostDirection(splitString[1]);
            } else if (checkForDirection(splitString[0])) {
                streetFinderAddress.setPreDirection(splitString[0]);
                suffixIndex = 1;
            }
        }
        streetFinderAddress.setStreetSuffix(splitString[suffixIndex]);
    }

    /**
     * Trims off the first part of a string
     * ex. TW-01 trims off the TW-
     * and returns 01
     * @param string
     * @return
     */
    private static String trim(String string) {
        return string.split("-")[1];
    }
}
