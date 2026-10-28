package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.district.AssignedDistricts;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.provider.district.LocalSource;
import lombok.Getter;

import java.util.List;
import java.util.Set;

import static gov.nysenate.sage.model.result.ResultStatus.SUCCESS;

/**
 * Represents the result returned by the district assignment service.
 */
@Getter
public class DistrictResult extends BaseResult<LocalSource> {
    private final AssignedDistricts assignedDistricts;

    public DistrictResult(List<LocalSource> sources, AssignedDistricts assignedDistricts) {
        this(sources, SUCCESS, assignedDistricts);
    }

    public DistrictResult(ResultStatus status) {
        this(null, status, AssignedDistricts.empty);
    }

    public DistrictResult(List<LocalSource> sources, ResultStatus status, AssignedDistricts assignedDistricts) {
        super(sources, status);
        this.assignedDistricts = assignedDistricts;
    }

    /** Accessor method to the set of assigned districts stored in DistrictInfo */
    public Set<DistrictType> getAssignedDistrictTypes() {
        return assignedDistricts.getAssignedTypes();
    }
}
