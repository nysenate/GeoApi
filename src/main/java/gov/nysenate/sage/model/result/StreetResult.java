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
        super(LocalSource.STREETFILE, districtedStreetRanges == null || districtedStreetRanges.isEmpty() ?
                ResultStatus.NO_STREET_LOOKUP_RESULT : ResultStatus.SUCCESS);
        this.districtedStreetRanges = districtedStreetRanges;
    }

    public List<DistrictedStreetRange> getDistrictedStreetRanges() {
        return districtedStreetRanges;
    }
}
