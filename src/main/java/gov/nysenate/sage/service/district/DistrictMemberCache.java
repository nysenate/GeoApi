package gov.nysenate.sage.service.district;

import gov.nysenate.sage.model.district.DistrictId;
import gov.nysenate.sage.model.district.DistrictMember;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.district.MemberInfo;

import java.util.Map;
import java.util.function.Function;

public class DistrictMemberCache extends DistrictIdCache<DistrictMember> {
    public DistrictMemberCache(Function<DistrictType, Map<DistrictId, DistrictMember>> supplier) {
        super(supplier);
    }

    @Override
    public DistrictMember getData(DistrictType type, DistrictId id) {
        DistrictMember result = super.getData(type, id);
        // We should only generate vacant members for real Senate seats.
        if (result == null && type == DistrictType.SENATE && id != null) {
            return new DistrictMember(new MemberInfo("Vacant", "District " + id,
                    "https://www.nysenate.gov/themes/custom/nysenate_theme/dist/images/nys_logo_header240x240.jpg",
                    "https://www.nysenate.gov/district/" + id, null), null);
        }
        return result;
    }
}
