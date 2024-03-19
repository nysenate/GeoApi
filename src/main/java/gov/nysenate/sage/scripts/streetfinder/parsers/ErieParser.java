package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;

import java.io.File;

import static gov.nysenate.sage.model.district.DistrictType.*;

public class ErieParser extends BaseParser {
    public ErieParser(File file) {
        super(file);
    }

    @Override
    protected StreetfileDataExtractor getDataExtractor() {
        return new StreetfileDataExtractor(ErieParser.class.getSimpleName())
                .addBuildingIndices(1, 2, 3).addStreetIndices(0).addPrecinctIndex(9)
                .addTypesInOrder(ZIP, TOWN).addType(SENATE, 10).addTypesInOrder(ASSEMBLY, COUNTY, CONGRESSIONAL);
    }
}
