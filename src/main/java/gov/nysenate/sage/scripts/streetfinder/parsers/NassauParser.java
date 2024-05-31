package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.dao.provider.district.MunicipalityType;
import gov.nysenate.sage.model.district.County;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;

import java.io.File;
import java.util.Map;

import static gov.nysenate.sage.model.district.DistrictType.*;

public class NassauParser extends CountyParser {
    public NassauParser(File file, Map<MunicipalityType, Map<String, Integer>> typeAndNameToIdMap, County county) {
        super(file, typeAndNameToIdMap, county);
    }

    @Override
    protected StreetfileDataExtractor getDataExtractor() {
        return super.getDataExtractor()
                .addBuildingIndices(3, 4, 5).addStreetIndices(1).addPrecinctIndex(0)
                .addType(TOWN_CITY, 2).addType(ZIP, 3).addType(CONGRESSIONAL, 8)
                .addTypesInOrder(SENATE, ASSEMBLY, CLEG);
    }
}
