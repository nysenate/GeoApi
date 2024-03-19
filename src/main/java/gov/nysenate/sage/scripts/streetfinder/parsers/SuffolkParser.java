package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;

import java.io.File;

import static gov.nysenate.sage.model.district.DistrictType.*;

/**
 * Parses Suffolk County txt file and outputs a tsv file
 */
public class SuffolkParser extends BaseParser {
    public SuffolkParser(File file) {
        super(file);
    }

    @Override
    protected StreetfileDataExtractor getDataExtractor() {
        // TODO: careful that the suffix isn't part of the name
        // TODO: also has apartments
        return new StreetfileDataExtractor(SuffolkParser.class.getSimpleName())
                .addBuildingIndices(6, 7, 8).addStreetIndices(2, 3, 4, 5)
                .addType(TOWN, 13)
                .addTypesInOrder(ELECTION, CONGRESSIONAL, SENATE, ASSEMBLY, CLEG, COUNTY, FIRE, VILLAGE);
    }

    @Override
    protected String[] parseLine(String line) {
        String[] splitLine = line.split("\t");
        // The street name comes first, but we need to try and set the suffix first.
        // TODO: could parse street normally and check this later?
        String temp3 = splitLine[3];
        if (temp3.contains("DO NOT USE MAIL")) {
            return null;
        }
        splitLine[3] = splitLine[4];
        splitLine[4] = temp3;
        if (19 > splitLine.length) {
            badLines.put("", line);
            return null;
        }
        return splitLine;
    }
}
