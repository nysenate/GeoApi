package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import static gov.nysenate.sage.model.address.StreetFileField.*;

/**
 * Parses Essex County 2018 csv file and converts to tsv file
 * Looks for town, street, ed, low, high, range type, asm, cong, sen, zip
 */
public class EssexParser extends BaseParser {
    /**
     * Calls the super constructor which sets up the tsv file
     * @param file
     * @throws IOException
     */
    public EssexParser(String file) throws IOException {
        super(file);
    }

    /**
     * Parses the line by calling all helper methods and saves data to a StreetFinderAddress
     * @param line
     */
    @Override
    protected void parseLine(String line) {
        ArrayList<String> split = new ArrayList<>(List.of(line.split(",")));
        // Two parts must be combined into something usable.
        split.set(5, getRangeType(split.get(5), split.remove(6)));

        List<BiConsumer<StreetFinderAddress, String>> functions = new ArrayList<>();
        functions.add(function(TOWN));
        functions.add(EssexParser::getStreetAndSuffix);
        functions.add(StreetFinderAddress::setED);
        functions.addAll(buildingFunctions);
        functions.add(function(ASSEMBLY));
        functions.add(function(CONGRESSIONAL));
        functions.add(function(SENATE));
        // TODO: might not exist
        functions.add(EssexParser::getZip);

        parseLineFun(functions, String.join(",", split));
    }

    /**
     * Gets the street name and street suffix
     * checks for pre-direction
     */
    private static void getStreetAndSuffix(StreetFinderAddress streetFinderAddress, String streetData) {
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
     * @param zip
     * @param streetFinderAddress
     */
    private static void getZip(StreetFinderAddress streetFinderAddress, String zip) {
        if (!zip.equals("M")) {
            streetFinderAddress.put(ZIP, zip);
        }
    }
}
