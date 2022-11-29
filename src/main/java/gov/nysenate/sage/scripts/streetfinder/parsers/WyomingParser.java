package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;
import gov.nysenate.sage.model.district.DistrictType;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Parses Wyoming County 2018 .txt
 * Looks for townCode, pre-Direction, Street, street suffix, post-direction, city, low,high, range type, and zip
 * Parses using the location of each column in the line
 */
public class WyomingParser extends NTSParser{
    /**
     * Calls the super constructor which sets up the tsv file
     * @param file
     * @throws IOException
     */
    public WyomingParser(String file) throws IOException {
        super(file);
    }


    /**
     * Parses the file for the needed information. Finds where the actual data is and then
     * parseLine is called to parse the line
     * @throws FileNotFoundException
     */
    public void parseFile() throws IOException {
        super.readFile();
    }

    /**
     * Parses the line by calling all necessary helper methods
     * @param line
     */
    protected void parseLine(String line) {
        if (line.contains("ODD_EVEN")) {
            return;
        }
        var streetFinderAddress = new StreetFinderAddress();
        String[] splitLine = line.split(",");
        // TODO: The 0 and 1 indices might be the town and district, respectively
        streetFinderAddress.setPreDirection(splitLine[2]);
        streetFinderAddress.setStreet(splitLine[3]);
        streetFinderAddress.setStreetSuffix(splitLine[4]);
        streetFinderAddress.setPostDirection(splitLine[5]);
        streetFinderAddress.setBuilding(true, splitLine[6]);
        streetFinderAddress.setBuilding(false, splitLine[7]);
        streetFinderAddress.setBldgParity(splitLine[8]);
        // TODO: might be city?
        streetFinderAddress.setTown(splitLine[9]);
        streetFinderAddress.setZip(splitLine[10]);
        handlePrecinct(splitLine[8], streetFinderAddress);
        streetFinderAddress.put(DistrictType.CONGRESSIONAL, split(splitLine[12]));
        streetFinderAddress.put(DistrictType.SENATE, split(splitLine[13]));
        streetFinderAddress.put(DistrictType.ASSEMBLY, splitLine[14]);
        writeToFile(streetFinderAddress);
    }

    private static String split(String input) {
        var split = input.split("-");
        return split.length > 1 ? split[1] : input;
    }
}
