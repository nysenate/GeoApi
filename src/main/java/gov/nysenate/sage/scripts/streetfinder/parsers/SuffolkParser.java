package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;
import gov.nysenate.sage.model.address.SuffolkStreetAddress;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import static gov.nysenate.sage.model.address.StreetFileField.*;

/**
 * Parses Suffolk County txt file and outputs a tsv file
 */
public class SuffolkParser extends BaseParser<SuffolkStreetAddress> {
    private static final List<BiConsumer<SuffolkStreetAddress, String>> secondaryBuildingFunctions = List.of(
            (ssfa, s) -> ssfa.setSecondaryBuilding(true, s),
            (ssfa, s) -> ssfa.setSecondaryBuilding(false, s),
            SuffolkStreetAddress::setSecondaryBuildingParity
    );

    /**
     * Calls the super constructor to set up tsv output file
     * @param file
     * @throws IOException
     */
    public SuffolkParser(String file) throws IOException {
        super(file);
    }

    @Override
    protected SuffolkStreetAddress getNewAddress() {
        return new SuffolkStreetAddress();
    }

    @Override
    protected List<BiConsumer<SuffolkStreetAddress, String>> getFunctions() {
        List<BiConsumer<SuffolkStreetAddress, String>> functions = new ArrayList<>();
        functions.add(function(ZIP));
        // skipping zip +4
        functions.addAll(skip(1));
        functions.add(StreetFinderAddress::setPreDirection);
        functions.add(StreetFinderAddress::setStreetSuffix);
        functions.add(SuffolkParser::getStreetName);
        functions.add(StreetFinderAddress::setPostDirection);
        functions.addAll(buildingFunctions);
        // secondary name
        functions.addAll(skip(1));
        functions.addAll(secondaryBuildingFunctions);
        functions.addAll(functions(TOWN, ELECTION_CODE, CONGRESSIONAL, SENATE, ASSEMBLY, CLEG, COUNTY_CODE, FIRE, VILLAGE));
        return functions;
    }

    /**
     * Parses the line by calling each helper method necessary for each line
     * @param line
     */
    @Override
    protected void parseLine(String line) {
        String[] splitLine = line.split("\t");
        // The street name comes first, but we need to try and set the suffix first.
        String temp3 = splitLine[3];
        if (temp3.contains("DO NOT USE MAIL")) {
            return;
        }
        splitLine[3] = splitLine[4];
        splitLine[4] = temp3;
        super.parseLine(String.join(",", List.of(splitLine)), 19);
    }

    /**
     * Gets the street Name and checks for a street suffix if StreetSuffix is empty
     */
    private static void getStreetName(StreetFinderAddress streetFinderAddress, String streetData) {
        LinkedList<String> splitList = new LinkedList<>(List.of(streetData.split(" ")));
        if (streetFinderAddress.getStreetSuffix().isEmpty() && splitList.size() > 1) {
            streetFinderAddress.setStreetSuffix(splitList.removeLast());
        }
        streetFinderAddress.setStreet(String.join(" ", splitList).trim());
    }
}
