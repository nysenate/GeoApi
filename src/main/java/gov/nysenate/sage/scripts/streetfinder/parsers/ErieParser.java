package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;
import gov.nysenate.sage.model.district.DistrictType;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import static gov.nysenate.sage.model.address.StreetFileField.*;

/**
 * Parses Erie County 2018 csv and puts parsed data into a tsv file
 * Looks for street, low, high, range type, townCode, District, zip
 */
public class ErieParser extends BaseParser<StreetFinderAddress> {
    /**
     * Calls the super constructor which sets up the tsv file
     * @param file
     * @throws IOException
     */
    public ErieParser(String file) throws IOException {
        super(file);
    }

    @Override
    protected StreetFinderAddress getNewAddress() {
        return new StreetFinderAddress();
    }

    @Override
    protected List<BiConsumer<StreetFinderAddress, String>> getFunctions() {
        List<BiConsumer<StreetFinderAddress, String>> functions = new ArrayList<>();
        functions.add(ErieParser::getStreetAndSuffix);
        functions.addAll(buildingFunctions);
        functions.addAll(functions(ZIP, TOWN));
        functions.addAll(skip(3));
        functions.add(handlePrecinct);
        functions.addAll(functions(true, SENATE, ASSEMBLY, COUNTY_CODE, CONGRESSIONAL));
        return functions;
    }

    /**
     * Gets the Street name and Street Suffix from a string containing both
     * Also checks for  pre-Direction
     * @param splitLine
     * @param streetFinderAddress
     */
    private static void getStreetAndSuffix(StreetFinderAddress streetFinderAddress, String splitLine) {
        LinkedList<String> splitList = new LinkedList<>(List.of(splitLine.split("\\s+")));
        if (checkForDirection(splitList.getFirst())) {
            streetFinderAddress.setPreDirection(splitList.removeFirst());
        }
        streetFinderAddress.setStreet(String.join(" ", splitList).trim());
        streetFinderAddress.setStreetSuffix(splitList.getLast());
    }
}
