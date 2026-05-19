package gov.nysenate.sage.client.view.district;

import gov.nysenate.sage.model.district.DistrictMember;

public class MemberDistrictView extends DistrictView {
    protected MemberView member;

    public MemberDistrictView(DistrictView baseView, DistrictMember districtMember) {
        super(baseView.getName(), baseView.getDistrict(), baseView.getDisplayName(), baseView.getMap());
        this.member = new MemberView(districtMember);
    }

    public MemberView getMember() {
        return (member != null && member.name != null) ? member : null;
    }
}
