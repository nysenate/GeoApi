package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.district.County;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;

import java.io.File;

import static gov.nysenate.sage.model.district.DistrictType.*;

/**
 * Parses Westchester County 2018 file
 */
public class WestchesterParser extends CountyParser {
    public WestchesterParser(File file, County county) {
        super(file, county);
    }

    @Override
    protected StreetfileDataExtractor getDataExtractor() {
        return super.getDataExtractor()
                .addBuildingIndices(6, 7, 8).addStreetIndices(2, 3, 4, 5)
                .addType(TOWN, 0).addPrecinctIndex(1).addType(ZIP, 9)
                .addTypesInOrder(CONGRESSIONAL, SENATE, ASSEMBLY, CLEG);
    }
}
