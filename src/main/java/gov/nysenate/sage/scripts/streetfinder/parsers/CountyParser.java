package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.dao.provider.district.MunicipalityType;
import gov.nysenate.sage.model.district.County;
import gov.nysenate.sage.scripts.streetfinder.model.StreetfileType;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Map;

public abstract class CountyParser extends BaseParser {
    private final County county;

    public CountyParser(File file, Map<MunicipalityType, Map<String, Integer>> typeAndNameToIdMap, County county) {
        super(file, typeAndNameToIdMap);
        this.county = county;
    }

    @Nonnull
    @Override
    public StreetfileType type() {
        return StreetfileType.COUNTY;
    }

    @Override
    protected StreetfileDataExtractor getDataExtractor() {
        return super.getDataExtractor().addCountyFunction(lineParts -> county.fipsCode());
    }
}
