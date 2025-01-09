package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.district.County;
import gov.nysenate.sage.scripts.streetfinder.model.StreetfileType;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;

import javax.annotation.Nonnull;
import java.io.File;

public abstract class CountyParser extends BaseParser {
    private final County county;

    public CountyParser(File file, County county) {
        super(file);
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
