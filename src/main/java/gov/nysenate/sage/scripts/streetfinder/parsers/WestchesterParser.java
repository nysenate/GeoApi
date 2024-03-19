package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;

import java.io.File;

import static gov.nysenate.sage.model.district.DistrictType.*;

/**
 * Parses Westchester County 2018 file
 */
public class WestchesterParser extends BaseParser {
    public WestchesterParser(File file) {
        super(file);
    }

    @Override
    protected StreetfileDataExtractor getDataExtractor() {
        return new StreetfileDataExtractor(WestchesterParser.class.getSimpleName())
                .addBuildingIndices(6, 7, 8).addStreetIndices(2, 3, 4, 5)
                .addType(TOWN, 0).addPrecinctIndex(1).addType(ZIP, 9)
                .addTypesInOrder(CONGRESSIONAL, SENATE, ASSEMBLY, CLEG);
    }
}
