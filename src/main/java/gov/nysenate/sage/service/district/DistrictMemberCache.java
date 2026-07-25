package gov.nysenate.sage.service.district;

import gov.nysenate.sage.model.district.DistrictMember;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.district.MemberInfo;

import java.util.Map;
import java.util.function.Function;

public class DistrictMemberCache extends DistrictCodeCache<DistrictMember> {
    public DistrictMemberCache(Function<DistrictType, Map<String, DistrictMember>> supplier) {
        super(supplier);
    }

    @Override
    public DistrictMember getData(DistrictType type, String code) {
        DistrictMember result = super.getData(type, code);
        // We should only generate vacant members for real Senate seats.
        if (result == null && type == DistrictType.SENATE && code != null) {
            return new DistrictMember(new MemberInfo("Vacant", "District " + code,
                    "https://www.nysenate.gov/themes/custom/nysenate_theme/dist/images/nys_logo_header240x240.jpg",
                    "https://www.nysenate.gov/district/" + code, null), null);
        }
        return result;
    }
}
