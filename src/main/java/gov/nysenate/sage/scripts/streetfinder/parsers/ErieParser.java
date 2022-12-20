package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

/**
 * Parses Erie County 2018 csv and puts parsed data into a tsv file
 */
public class ErieParser extends BasicParser {
    public ErieParser(String file) throws IOException {
        super(file);
    }

    @Override
    protected List<BiConsumer<StreetFileAddress, String>> getFunctions() {
        List<BiConsumer<StreetFileAddress, String>> functions = new ArrayList<>();
        functions.add(ErieParser::setStreetAndSuffix);
        functions.addAll(buildingFunctions);
        functions.addAll(functions(ZIP, TOWN));
        functions.addAll(skip(3));
        functions.add(handlePrecinct);
        functions.addAll(functions(true, SENATE, ASSEMBLY, COUNTY_ID, CONGRESSIONAL));
        return functions;
    }

    /**
     * Gets the Street name and Street Suffix from a string containing both
     * Also checks for  pre-Direction
     * @param splitLine
     * @param streetFileAddress
     */
    private static void setStreetAndSuffix(StreetFileAddress streetFileAddress, String splitLine) {
        LinkedList<String> splitList = new LinkedList<>(List.of(splitLine.split("\\s+")));
        streetFileAddress.setStreetSuffix(splitList.removeLast());
        streetFileAddress.setStreet(String.join(" ", splitList).trim());
    }
}
