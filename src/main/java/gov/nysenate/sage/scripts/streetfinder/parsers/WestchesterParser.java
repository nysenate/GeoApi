package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static gov.nysenate.sage.model.address.StreetFileField.*;

/**
 * Parses Westchester County 2018 file
 * Looks for town, pre-Direction, street, street suffix, post-direction, low, high, range type, zip, skips CNG, sen, asm, dist
 */
public class WestchesterParser extends BaseParser {

    /**
     * Calls the super constructor which sets up the tsv file
     * @param file
     * @throws IOException
     */
    public WestchesterParser(String file) throws IOException {
        super(file);
    }

    @Override
    /**
     * Parses the line by calling each helper method to extract all data
     * @param line
     */
    protected void parseLine(String line) {
        List<BiConsumer<StreetFinderAddress, String>> functions = new ArrayList<>();
        functions.add(function(TOWN));
        functions.add(handlePrecinct);
        functions.add(StreetFinderAddress::setPreDirection);
        functions.add(StreetFinderAddress::setStreet);
        functions.add(StreetFinderAddress::setStreetSuffix);
        functions.add(StreetFinderAddress::setPostDirection);
        functions.addAll(buildingFunctions);
        functions.add(function(ZIP));
        functions.add(function(CONGRESSIONAL, true));
        functions.add(function(SENATE, true));
        functions.add(function(ASSEMBLY, true));
        functions.add(function(CLEG, true));
        parseLineFun(functions, line);
    }
}
