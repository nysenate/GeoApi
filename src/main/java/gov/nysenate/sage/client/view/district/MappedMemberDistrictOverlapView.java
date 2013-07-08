package gov.nysenate.sage.client.view.district;

import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictOverlap;

public class MappedMemberDistrictOverlapView extends MappedDistrictOverlapView
{
    protected MemberView member;

    public MappedMemberDistrictOverlapView(DistrictOverlap districtOverlap, String districtCode, DistrictMatchLevel matchLevel) {
        super(districtOverlap, districtCode, matchLevel);
        if (districtOverlap != null && districtCode != null) {
            member = new MemberView(districtOverlap.getTargetDistrictMember(districtCode));
        }
    }

    public MemberView getMember() {
        return member;
    }
}
