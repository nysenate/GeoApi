package gov.nysenate.sage.client.view.district;

import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictMember;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.district.SingleDistrict;

public class MemberDistrictView extends DistrictView {
    private final MemberView member;

    public MemberDistrictView(SingleDistrict data, DistrictType type, DistrictMap map, DistrictMember member) {
        super(data, type, map);
        this.member = MemberView.from(member);
    }

    public MemberView getMember() {
        return member;
    }
}
