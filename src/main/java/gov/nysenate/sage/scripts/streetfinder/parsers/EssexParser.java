package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

/**
 * Parses Essex County 2018 csv file and converts to tsv file
 */
public class EssexParser extends BasicParser {
    public EssexParser(String file) throws IOException {
        super(file);
    }

    @Override
    protected List<BiConsumer<StreetFileAddress, String>> getFunctions() {
        List<BiConsumer<StreetFileAddress, String>> functions = new ArrayList<>();
        functions.add(function(TOWN));
        functions.add(EssexParser::setStreetAndSuffix);
        functions.add(function(ELECTION_CODE));
        functions.addAll(buildingFunctions);
        functions.addAll(functions(ASSEMBLY, CONGRESSIONAL, SENATE));
        // TODO: might not exist
        functions.add(EssexParser::getZip);
        return functions;
    }


    /**
     * This type has a strange way of showing parity.
     */
    @Override
    protected void parseLine(String line) {
        ArrayList<String> split = new ArrayList<>(List.of(line.split(",")));
        // Two parts must be combined into something usable.
        split.set(5, getRangeType(split.get(5), split.remove(6)));
        super.parseLine(String.join(",", split));
    }

    /**
     * Gets the street name and street suffix
     * checks for pre-direction
     */
    private static void setStreetAndSuffix(StreetFileAddress streetFileAddress, String streetData) {
        LinkedList<String> splitList = new LinkedList<>(List.of(streetData.split(" ")));
        streetFileAddress.setStreetSuffix(splitList.removeLast());
        streetFileAddress.setStreet(String.join(" ", splitList).trim());
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
     * @param streetFileAddress
     */
    private static void getZip(StreetFileAddress streetFileAddress, String zip) {
        if (!zip.equals("M")) {
            streetFileAddress.put(ZIP, zip);
        }
    }
}
