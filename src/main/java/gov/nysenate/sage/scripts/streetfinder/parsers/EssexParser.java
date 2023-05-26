package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;
import gov.nysenate.sage.scripts.streetfinder.model.StreetParity;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

/**
 * Parses Essex County 2018 csv file and converts to tsv file
 */
public class EssexParser extends BasicParser {
    public EssexParser(File file) {
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
        functions.add((sfa, str) -> {
            if (!str.equals("M")) {
                sfa.put(ZIP, str);
            }});
        return functions;
    }


    /**
     * This type has a strange way of showing parity.
     */
    @Override
    protected void parseLine(String line) {
        List<String> split = new ArrayList<>(List.of(line.split(",")));
        // Two parts must be combined into something usable.
        String firstPart = split.get(5);
        String secondPart = split.remove(6);
        StreetParity parity;
        if (firstPart.equals("-1") && secondPart.equals("0")) {
            parity = StreetParity.ODDS;
        } else if (firstPart.equals("0") && secondPart.equals("-1")) {
            parity = StreetParity.EVENS;
        } else {
            parity = StreetParity.ALL;
        }
        split.set(5, parity.name());
        super.parseLine(split);
    }

    /**
     * Gets the street name and street suffix
     * checks for pre-direction
     */
    private static void setStreetAndSuffix(StreetFileAddress streetFileAddress, String streetData) {
        LinkedList<String> splitList = new LinkedList<>(List.of(streetData.split(" ")));
        streetFileAddress.setStreetSuffix(splitList.removeLast());
        streetFileAddress.put(STREET, String.join(" ", splitList));
    }
}
