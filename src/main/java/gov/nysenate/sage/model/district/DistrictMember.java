package gov.nysenate.sage.model.district;

import gov.nysenate.services.model.Office;
import gov.nysenate.services.model.Senator;

import java.util.List;

/**
 * Represents a generic member associated with a district number.
 */
public record DistrictMember(MemberInfo info, List<OfficeInfo> offices) {
    public DistrictMember(Senator senator, List<Office> senateOffices) {
        this(new MemberInfo(senator), senateOffices.stream().map(OfficeInfo::new).toList());
    }
}
