package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.provider.district.LocalSource;

import java.util.Set;

import static gov.nysenate.sage.model.result.ResultStatus.SUCCESS;

/**
 * Represents the result returned by the district assignment service.
 */
public class DistrictResult extends BaseResult<LocalSource> {
    private final DistrictInfo districtInfo;

    public DistrictResult(LocalSource source, DistrictInfo districtInfo) {
        this(source, SUCCESS, districtInfo);
    }

    public DistrictResult(LocalSource source, ResultStatus status) {
        this(source, status, DistrictInfo.empty);
    }

    public DistrictResult(LocalSource source, ResultStatus status, DistrictInfo districtInfo) {
        super(source);
        this.statusCode = status;
        this.districtInfo = districtInfo;
        setResultTime();
    }

    public DistrictInfo getDistrictInfo() {
        return districtInfo;
    }

    /** Accessor method to the set of assigned districts stored in DistrictInfo */
    public Set<DistrictType> getAssignedDistricts() {
        return getDistrictInfo() == null ? Set.of() : getDistrictInfo().typeToDistrictMap().keySet();
    }
}
