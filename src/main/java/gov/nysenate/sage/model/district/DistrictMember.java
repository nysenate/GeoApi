package gov.nysenate.sage.model.district;

import gov.nysenate.services.model.Office;
import gov.nysenate.services.model.Senator;

import java.util.Collection;
import java.util.List;

/**
 * Represents a generic member associated with a district number.
 */
public record DistrictMember(MemberInfo info, List<OfficeInfo> offices) {
    public DistrictMember(Senator senator, List<Office> senateOffices) {
        this(new MemberInfo(senator), senateOffices.stream().map(OfficeInfo::new).toList());
    }

    public DistrictMember(MemberInfo info, Collection<OfficeInfo> offices) {
        this(info, offices.stream().toList());
    }
}
