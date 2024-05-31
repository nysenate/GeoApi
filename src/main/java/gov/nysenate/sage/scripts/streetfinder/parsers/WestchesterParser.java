package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.dao.provider.district.MunicipalityType;
import gov.nysenate.sage.model.district.County;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;

import java.io.File;
import java.util.Map;

import static gov.nysenate.sage.model.district.DistrictType.*;

/**
 * Parses Westchester County 2018 file
 */
public class WestchesterParser extends CountyParser {
    public WestchesterParser(File file, Map<MunicipalityType, Map<String, Integer>> typeAndNameToIdMap, County county) {
        super(file, typeAndNameToIdMap, county);
    }

    @Override
    protected StreetfileDataExtractor getDataExtractor() {
        return super.getDataExtractor()
                .addBuildingIndices(6, 7, 8).addStreetIndices(2, 3, 4, 5)
                .addType(TOWN_CITY, 0).addPrecinctIndex(1).addType(ZIP, 9)
                .addTypesInOrder(CONGRESSIONAL, SENATE, ASSEMBLY, CLEG);
    }
}
