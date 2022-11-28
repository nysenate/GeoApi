package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;
import gov.nysenate.sage.model.district.DistrictType;

import java.io.IOException;

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
        streetFinderAddress.setBuilding(true, false, splitLine[5]);
        streetFinderAddress.setBuilding(false, false, splitLine[6]);
        streetFinderAddress.setBldg_parity(getRangeType(splitLine[7]));
        // TODO: No use of the 8th part?
        streetFinderAddress.put(DistrictType.CONGRESSIONAL, trim(splitLine[9]));
        streetFinderAddress.put(DistrictType.SENATE, trim(splitLine[10]));
        streetFinderAddress.put(DistrictType.ASSEMBLY, trim(splitLine[11]));
        streetFinderAddress.put(DistrictType.CLEG, trim(splitLine[12]));
        // Ignore TD
        writeToFile(streetFinderAddress);
    }

    private void handlePrecinct(String precinct, StreetFinderAddress streetFinderAddress) {
        if (precinct.length() == 5) {
            precinct = "0" + precinct;
        }
        streetFinderAddress.setTownCode(precinct.substring(0, 2));
        streetFinderAddress.put(DistrictType.WARD, precinct.substring(2, 4));
        streetFinderAddress.setED(precinct.substring(precinct.length() - 2));

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

    // TODO: is repeated elsewhere
    private static String getRangeType(String range) {
        if (range.equals("O")) {
            return "ODDS";
        } else if (range.equals("E")) {
            return "EVENS";
        }
        return "ALL";
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
