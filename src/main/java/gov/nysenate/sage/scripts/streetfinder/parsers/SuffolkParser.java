package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileFunctionList;
import gov.nysenate.sage.scripts.streetfinder.model.SuffolkStreetFileAddress;

import java.io.File;
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
    protected StreetFileFunctionList<SuffolkStreetFileAddress> getFunctions() {
        return new StreetFileFunctionList<SuffolkStreetFileAddress>()
                .addFunctions(false, ZIP).skip(1)
                // TODO: careful that the suffix isn't part of the name
                .addStreetParts(4).addFunctions(buildingFunctions)
                .skip(1).addFunctions(secondaryBuildingFunctions)
                .addFunctions(false, TOWN, ELECTION_CODE, CONGRESSIONAL,
                        SENATE, ASSEMBLY, CLEG, COUNTY_ID, FIRE, VILLAGE);
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
        if (19 > splitLine.length) {
            putBadLine("", line);
            return;
        }
        parseData(splitLine);
    }
}
