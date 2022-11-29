package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static gov.nysenate.sage.model.address.StreetFileField.*;

/**
 * Parses Essex County 2018 csv file and converts to tsv file
 * Looks for town, street, ed, low, high, range type, asm, cong, sen, zip
 */
public class EssexParser extends NTSParser {
    /**
     * Calls the super constructor which sets up the tsv file
     * @param file
     * @throws IOException
     */
    public EssexParser(String file) throws IOException {
        super(file);
    }

    /**
     * Parse the file by calling parseLine for each line of data
     * @throws FileNotFoundException
     */
    public void parseFile() throws IOException {
        super.readFile();
    }

    /**
     * Parses the line by calling all helper methods and saves data to a StreetFinderAddress
     * @param line
     */
    @Override
    protected void parseLine(String line) {
        StreetFinderAddress streetFinderAddress = new StreetFinderAddress();
        String[] splitLine = line.split(",");
        streetFinderAddress.setTown(splitLine[0]);
        getStreetAndSuffix(splitLine[1], streetFinderAddress);
        streetFinderAddress.setED(splitLine[2]);
        streetFinderAddress.setBuilding(true, splitLine[3]);
        streetFinderAddress.setBuilding(false, splitLine[4]);
        streetFinderAddress.setBldgParity(getRangeType(splitLine[5], splitLine[6]));
        streetFinderAddress.put(ASSEMBLY, splitLine[7]);
        streetFinderAddress.put(CONGRESSIONAL, splitLine[8]);
        streetFinderAddress.put(SENATE, splitLine[9]);
        getZip(splitLine, streetFinderAddress);
        super.writeToFile(streetFinderAddress);
    }

    /**
     * Gets the street name and street suffix
     * checks for pre-direction
     */
    private void getStreetAndSuffix(String streetData, StreetFinderAddress streetFinderAddress) {
        LinkedList<String> splitList = new LinkedList<>(List.of(streetData.split(" ")));
        if (checkForDirection(splitList.getFirst())) {
            streetFinderAddress.setPreDirection(splitList.removeFirst());
        }
        streetFinderAddress.setStreet(String.join(" ", splitList).trim());
        streetFinderAddress.setStreetSuffix(splitList.getLast());
    }

    /**
     * Gets the range type and converts it to correct format
     */
    private static String getRangeType(String firstPart, String secondPart) {
        if (firstPart.equals("-1") && secondPart.equals("0")) {
            return "ODDS";
        } else if (firstPart.equals("0") && secondPart.equals("-1")) {
            return "EVENS";
        }
        return "ALL";
    }

    /**
     * Gets the zip code
     * @param splitLine
     * @param streetFinderAddress
     */
    private void getZip(String[] splitLine, StreetFinderAddress streetFinderAddress) {
        if (splitLine.length > 10 && !splitLine[10].equals("M")) {
            //Special case that only occurs a couple of times
            streetFinderAddress.setZip(splitLine[10]);
        }
    }
}
