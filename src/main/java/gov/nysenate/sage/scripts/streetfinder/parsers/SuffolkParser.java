package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;
import gov.nysenate.sage.model.address.SuffolkStreetAddress;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static gov.nysenate.sage.model.address.StreetFileField.*;

/**
 * Parses Suffolk County txt file and outputs a tsv file
 */
public class SuffolkParser extends BaseParser {
    //-22 categories seperated by tabs (21 tabs not counting 1st category)
    //- zip, zip+4, pre, street name, street mode, post, low, high, odd/even, secondary name, secondary low, secondary high, secondary odd/even, town, ed, cong, sen, asm, cleg, dist, fire, vill
    /**
     * Calls the super constructor to set up tsv output file
     * @param file
     * @throws IOException
     */
    public SuffolkParser(String file) throws IOException {
        super(file);
    }

    /**
     * Parses the line by calling each helper method necessary for each line
     * @param line
     */
    @Override
    protected void parseLine(String line) {
        var streetFinderAddress = new SuffolkStreetAddress();
        String[] splitLine = line.split("\t");

        streetFinderAddress.put(ZIP, splitLine[0]);
        // skip zip +4, splitLine[1]
        streetFinderAddress.setPreDirection(splitLine[2]);
        // Getting suffix first because the suffix might be in with the street name
        streetFinderAddress.setStreetSuffix(splitLine[4]);
        getStreetName(splitLine[3], streetFinderAddress);
        // TODO: what is this?
        if (streetFinderAddress.getStreet().contains("DO NOT USE MAIL"))
            return;
        streetFinderAddress.setPostDirection(splitLine[5]);
        streetFinderAddress.setBuilding(true, splitLine[6]);
        streetFinderAddress.setBuilding(false, splitLine[7]);
        streetFinderAddress.setBldgParity(splitLine[8]);
        // skip over secondary name, splitLine[9]
        streetFinderAddress.setSecondaryBuilding(true, splitLine[10]);
        streetFinderAddress.setSecondaryBuilding(false, splitLine[11]);
        streetFinderAddress.setSecondaryBuildingParity(splitLine[12]);
        streetFinderAddress.put(TOWN, splitLine[13].trim());
        streetFinderAddress.setED(splitLine[14]);
        streetFinderAddress.put(CONGRESSIONAL, splitLine[15]);
        streetFinderAddress.put(SENATE, splitLine[16]);
        streetFinderAddress.put(ASSEMBLY, splitLine[17]);
        streetFinderAddress.put(CLEG, splitLine[18]);
        //Next categories don't always exist so checking the size of the line
        // TODO: always check length + maximum length + warning?
        if (splitLine.length > 19) {
            streetFinderAddress.put(COUNTY_CODE, splitLine[19]);
            if (splitLine.length > 20)
                streetFinderAddress.put(FIRE, splitLine[20]);
            if (splitLine.length > 21)
                streetFinderAddress.put(VILLAGE, splitLine[21]);
        }
        writeToFile(streetFinderAddress);
    }

    /**
     * Gets the street Name and checks for a street suffix if StreetSuffix is empty
     */
    private void getStreetName(String streetData, StreetFinderAddress streetFinderAddress) {
        LinkedList<String> splitList = new LinkedList<>(List.of(streetData.split(" ")));
        if (streetFinderAddress.getStreetSuffix().isEmpty() && splitList.size() > 1) {
            streetFinderAddress.setStreetSuffix(splitList.removeLast());
        }
        streetFinderAddress.setStreet(String.join(" ", splitList).trim());
    }
}
