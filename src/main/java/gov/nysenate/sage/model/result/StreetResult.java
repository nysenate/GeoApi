package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.address.DistrictedStreetRange;

import java.util.List;

/**
 * Represents the result of a street lookup from a street provider.
 */
public class StreetResult extends BaseResult
{
    protected List<DistrictedStreetRange> districtedStreetRanges;

    public StreetResult()
    {
        this(null, null);
    }

    public StreetResult(Class sourceClass)
    {
        this(sourceClass, null);
    }

    public StreetResult(Class sourceClass, ResultStatus resultStatus)
    {
        this.setSource(sourceClass);
        this.setStatusCode(resultStatus);
    }

    public List<DistrictedStreetRange> getDistrictedStreetRanges() {
        return districtedStreetRanges;
    }

    public void setDistrictedStreetRanges(List<DistrictedStreetRange> districtedStreetRanges) {
        this.districtedStreetRanges = districtedStreetRanges;
    }
}
