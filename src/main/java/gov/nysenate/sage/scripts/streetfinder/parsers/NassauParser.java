package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

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

    /**
     * Parses the line by calling each helper method to extract all data
     * @param line
     */
    @Override
    protected void parseLine(String line) {
        List<BiConsumer<StreetFinderAddress, String>> functions = new ArrayList<>();
        functions.add(handlePrecinct);
        functions.add(StreetFinderAddress::setStreet);
        functions.add(NassauParser::getSuffix);
        functions.add(function(TOWN));
        functions.add(function(ZIP));
        functions.addAll(buildingFunctions);
        functions.addAll(skip(1));
        functions.add(function(CONGRESSIONAL, true));
        functions.add(function(SENATE, true));
        functions.add(function(ASSEMBLY, true));
        functions.add(function(CLEG, true));
        parseLineFun(functions, line);
    }

    private static void getSuffix(StreetFinderAddress streetFinderAddress, String data) {
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
}
