package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;
import gov.nysenate.sage.scripts.streetfinder.model.SuffolkStreetFileAddress;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

/**
 * Parses Suffolk County txt file and outputs a tsv file
 */
public class SuffolkParser extends BaseParser<SuffolkStreetFileAddress> {
    private static final List<BiConsumer<SuffolkStreetFileAddress, String>> secondaryBuildingFunctions = List.of(
            (ssfa, s) -> ssfa.setSecondaryBuilding(true, s),
            (ssfa, s) -> ssfa.setSecondaryBuilding(false, s),
            SuffolkStreetFileAddress::setSecondaryBuildingParity
    );

    public SuffolkParser(String file) throws IOException {
        super(file);
    }

    @Override
    protected SuffolkStreetFileAddress getNewAddress() {
        return new SuffolkStreetFileAddress();
    }

    @Override
    protected List<BiConsumer<SuffolkStreetFileAddress, String>> getFunctions() {
        List<BiConsumer<SuffolkStreetFileAddress, String>> functions = new ArrayList<>();
        functions.add(function(ZIP));
        // skipping zip +4
        functions.add(skip);
        functions.add(StreetFileAddress::setPreDirection);
        functions.add(StreetFileAddress::setStreetSuffix);
        functions.add(SuffolkParser::getStreetName);
        functions.add(StreetFileAddress::setPostDirection);
        functions.addAll(buildingFunctions);
        // secondary name
        functions.add(skip);
        functions.addAll(secondaryBuildingFunctions);
        functions.addAll(functions(TOWN, ELECTION_CODE, CONGRESSIONAL, SENATE, ASSEMBLY, CLEG, COUNTY_CODE, FIRE, VILLAGE));
        return functions;
    }

    @Override
    protected void parseLine(String line) {
        String[] splitLine = line.split("\t");
        // The street name comes first, but we need to try and set the suffix first.
        // TODO: could parse street normally and check this later?
        String temp3 = splitLine[3];
        if (temp3.contains("DO NOT USE MAIL")) {
            return;
        }
        splitLine[3] = splitLine[4];
        splitLine[4] = temp3;
        super.parseLine(List.of(splitLine), 19);
    }

    /**
     * Gets the street Name and checks for a street suffix if StreetSuffix is empty
     */
    private static void getStreetName(StreetFileAddress streetFileAddress, String streetData) {
        LinkedList<String> splitList = new LinkedList<>(List.of(streetData.split(" ")));
        if (streetFileAddress.getStreetSuffix().isEmpty() && splitList.size() > 1) {
            streetFileAddress.setStreetSuffix(splitList.removeLast());
        }
        streetFileAddress.setStreet(String.join(" ", splitList).trim());
    }
}
