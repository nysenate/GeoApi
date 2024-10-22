package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.address.DistrictedStreetRange;
import gov.nysenate.sage.service.street.StreetData;

import java.util.List;

/**
 * Represents the result of a street lookup from a street provider.
 */
public class StreetResult extends BaseResult<StreetData> {
    protected List<DistrictedStreetRange> districtedStreetRanges;

    public StreetResult(StreetData source, List<DistrictedStreetRange> districtedStreetRanges) {
        super(source);
        this.districtedStreetRanges = districtedStreetRanges;
        this.statusCode = districtedStreetRanges == null ?
                ResultStatus.NO_STREET_LOOKUP_RESULT : ResultStatus.SUCCESS;
    }

    public List<DistrictedStreetRange> getDistrictedStreetRanges() {
        return districtedStreetRanges;
    }
}
