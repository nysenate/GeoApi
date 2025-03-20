package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.address.DistrictedStreetRange;
import gov.nysenate.sage.provider.district.LocalSource;

import java.util.List;

/**
 * Represents the result of a street lookup from a street provider.
 */
public class StreetResult extends BaseResult<LocalSource> {
    protected List<DistrictedStreetRange> districtedStreetRanges;

    public StreetResult(List<DistrictedStreetRange> districtedStreetRanges) {
        super(LocalSource.STREETFILE);
        this.districtedStreetRanges = districtedStreetRanges;
        this.statusCode = districtedStreetRanges == null ?
                ResultStatus.NO_STREET_LOOKUP_RESULT : ResultStatus.SUCCESS;
    }

    public List<DistrictedStreetRange> getDistrictedStreetRanges() {
        return districtedStreetRanges;
    }
}
