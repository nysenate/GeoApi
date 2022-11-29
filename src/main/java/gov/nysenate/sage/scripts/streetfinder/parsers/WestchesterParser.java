package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;
import gov.nysenate.sage.model.district.DistrictType;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Parses Westchester County 2018 file
 * Looks for town, pre-Direction, street, street suffix, post-direction, low, high, range type, zip, skips CNG, sen, asm, dist
 */
public class WestchesterParser extends NTSParser {

    /**
     * Calls the super constructor which sets up the tsv file
     * @param file
     * @throws IOException
     */
    public WestchesterParser(String file) throws IOException {
        super(file);
    }

    /**
     * Parses the file by calling parseLine for each line of data
     * @throws FileNotFoundException
     */
    public void parseFile() throws IOException {
        super.readFile();
    }

    @Override
    /**
     * Parses the line by calling each helper method to extract all data
     * @param line
     */
    protected void parseLine(String line) {
        StreetFinderAddress streetFinderAddress = new StreetFinderAddress();
        String[] splitLine = line.split(",");
        streetFinderAddress.setTown(splitLine[0]);
        handlePrecinct(splitLine[1], streetFinderAddress);
        streetFinderAddress.setPreDirection(splitLine[2]);
        streetFinderAddress.setStreet(splitLine[3]);
        streetFinderAddress.setStreetSuffix(splitLine[4]);
        streetFinderAddress.setPostDirection(splitLine[5]);
        streetFinderAddress.setBuilding(true, splitLine[6]);
        streetFinderAddress.setBuilding(false, splitLine[7]);
        streetFinderAddress.setBldgParity(splitLine[8]);
        streetFinderAddress.setZip(splitLine[9]);
        streetFinderAddress.put(DistrictType.CONGRESSIONAL, split(splitLine[10]));
        streetFinderAddress.put(DistrictType.SENATE, split(splitLine[11]));
        streetFinderAddress.put(DistrictType.ASSEMBLY, split(splitLine[12]));
        streetFinderAddress.put(DistrictType.CLEG, split(splitLine[13]));
        //ignore CNL-DT
        writeToFile(streetFinderAddress);
    }

    private static String split(String input) {
        var split = input.split("-");
        return split.length > 1 ? split[1] : input;
    }
}
