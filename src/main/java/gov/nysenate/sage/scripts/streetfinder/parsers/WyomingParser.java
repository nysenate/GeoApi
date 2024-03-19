package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;

import java.io.File;

import static gov.nysenate.sage.model.district.DistrictType.*;

/**
 * Parses Wyoming County 2018 .txt
 */
public class WyomingParser extends BaseParser {
    public WyomingParser(File file) {
        super(file);
    }

    @Override
    protected StreetfileDataExtractor getDataExtractor() {
        return new StreetfileDataExtractor(WyomingParser.class.getSimpleName())
                .addBuildingIndices(6, 7, 8).addStreetIndices(2, 3, 4, 5)
                .addType(TOWN, 9).addType(ZIP, 10).addPrecinctIndex(11)
                .addTypesInOrder(CONGRESSIONAL, SENATE, ASSEMBLY);
    }

    @Override
    protected String[] parseLine(String line) {
        if (line.contains("ODD_EVEN")) {
            return null;
        }
        return super.parseLine(line);
    }
}
