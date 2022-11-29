package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;
import gov.nysenate.sage.model.district.DistrictType;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static gov.nysenate.sage.model.address.StreetFileField.*;

/**
 * Parses Wyoming County 2018 .txt
 * Looks for townCode, pre-Direction, Street, street suffix, post-direction, city, low,high, range type, and zip
 * Parses using the location of each column in the line
 */
public class WyomingParser extends BaseParser {
    /**
     * Calls the super constructor which sets up the tsv file
     * @param file
     * @throws IOException
     */
    public WyomingParser(String file) throws IOException {
        super(file);
    }

    /**
     * Parses the line by calling all necessary helper methods
     * @param line
     */
    protected void parseLine(String line) {
        if (line.contains("ODD_EVEN")) {
            return;
        }
        // TODO: The 0 and 1 indices might be the town and district, respectively
        List<BiConsumer<StreetFinderAddress, String>> functions = new ArrayList<>(skip(2));
        functions.add(StreetFinderAddress::setPreDirection);
        functions.add(StreetFinderAddress::setStreet);
        functions.add(StreetFinderAddress::setStreetSuffix);
        functions.add(StreetFinderAddress::setPostDirection);
        functions.addAll(buildingFunctions);
        functions.add(function(TOWN));
        functions.add(function(ZIP));
        functions.add(handlePrecinct);
        functions.add(function(CONGRESSIONAL));
        functions.add(function(SENATE));
        functions.add(function(ASSEMBLY));
        parseLineFun(functions, line);
    }

    private static String split(String input) {
        var split = input.split("-");
        return split.length > 1 ? split[1] : input;
    }
}
