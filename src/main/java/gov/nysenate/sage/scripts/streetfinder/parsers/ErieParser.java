package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.dao.provider.district.MunicipalityType;
import gov.nysenate.sage.model.district.County;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;

import java.io.File;
import java.util.Map;

import static gov.nysenate.sage.model.district.DistrictType.*;

public class ErieParser extends CountyParser {
    public ErieParser(File file, Map<MunicipalityType, Map<String, Integer>> typeAndNameToIdMap, County county) {
        super(file, typeAndNameToIdMap, county);
    }

    @Override
    protected StreetfileDataExtractor getDataExtractor() {
        return super.getDataExtractor().addBuildingIndices(1, 2, 3)
                .addStreetIndices(0).addPrecinctIndex(9)
                .addTypesInOrder(ZIP, TOWN_CITY).addType(SENATE, 10)
                .addTypesInOrder(ASSEMBLY, COUNTY, CONGRESSIONAL);
    }
}
