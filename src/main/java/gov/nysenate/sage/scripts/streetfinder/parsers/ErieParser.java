package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;
import gov.nysenate.sage.model.district.DistrictType;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Parses Erie County 2018 csv and puts parsed data into a tsv file
 * Looks for street, low, high, range type, townCode, District, zip
 */
public class ErieParser extends NTSParser {

    /**
     * Calls the super constructor which sets up the tsv file
     * @param file
     * @throws IOException
     */
    public ErieParser(String file) throws IOException {
        super(file);
    }


    /**
     * Parses the file by calling parseLine for each line of data
     * @throws FileNotFoundException
     */
    public void parseFile() throws IOException {
       super.readFile();
    }

    /**
     * Parses the line by calling each method to find all the data given in the file
     * and add the data to the StreetFinderAddress
     * @param line
     */
    @Override
    protected void parseLine(String line) {
        StreetFinderAddress streetFinderAddress = new StreetFinderAddress();
        String[] splitLine = line.split(",");
        getStreetAndSuffix(splitLine[0], streetFinderAddress);
        streetFinderAddress.setBuilding(true, false, splitLine[1]);
        streetFinderAddress.setBuilding(false, false, splitLine[2]);
        streetFinderAddress.setBldg_parity(getRangeType(splitLine[3]));
        streetFinderAddress.setZip(splitLine[4]);
        streetFinderAddress.setTown(splitLine[5]);
        handlePrecinct(splitLine[9], streetFinderAddress);
        streetFinderAddress.put(DistrictType.SENATE, split(splitLine[10]));
        streetFinderAddress.put(DistrictType.ASSEMBLY, split(splitLine[11]));
        streetFinderAddress.setDist(split(splitLine[12]));
        streetFinderAddress.put(DistrictType.CONGRESSIONAL, split(splitLine[13]));
        writeToFile(streetFinderAddress);
    }

    private static String split(String input) {
        var split = input.split("-");
        return split.length > 1 ? split[1] : input;
    }

    private void handlePrecinct(String precinct, StreetFinderAddress streetFinderAddress) {
        // Add a leading zero if only 5 digits
        if (precinct.length() == 5) {
            precinct = "0" + precinct;
        }
        streetFinderAddress.setTownCode(precinct.substring(0, 2));
        streetFinderAddress.put(DistrictType.WARD, precinct.substring(2, 4));
        streetFinderAddress.setED(precinct.substring(precinct.length() - 2));

    }

    /**
     * Gets the Street name and Street Suffix from a string containing both
     * Also checks for  pre-Direction
     * @param splitLine
     * @param streetFinderAddress
     */
    private void getStreetAndSuffix(String splitLine, StreetFinderAddress streetFinderAddress) {
        LinkedList<String> splitList = new LinkedList<>(List.of(splitLine.split("\\s+")));
        if (checkForDirection(splitList.getFirst())) {
            streetFinderAddress.setPreDirection(splitList.removeFirst());
                    }

        streetFinderAddress.setStreet(String.join(" ", splitList).trim());
        streetFinderAddress.setStreetSuffix(splitList.getLast());
    }

    /**
     * Gets the Range Type and converts to standard formatting
     * @param splitLine
     */
    private String getRangeType(String splitLine) {
        if (splitLine.matches("Even|Odd")) {
            return splitLine.toUpperCase() + "S";
        }
        return "ALL";
    }
}
