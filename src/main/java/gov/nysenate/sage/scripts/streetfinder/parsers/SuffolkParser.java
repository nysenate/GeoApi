package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.SuffolkStreetFileAddress;

import java.io.File;
import java.util.ArrayList;
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

    public SuffolkParser(File file) {
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
        // TODO: careful that the suffix isn't part of the name
        functions.addAll(streetParts(4));
        functions.addAll(buildingFunctions);
        // secondary name
        functions.add(skip);
        functions.addAll(secondaryBuildingFunctions);
        functions.addAll(functions(TOWN, ELECTION_CODE, CONGRESSIONAL, SENATE, ASSEMBLY, CLEG, COUNTY_ID, FIRE, VILLAGE));
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
}
