package gov.nysenate.sage.client.view.district;

import gov.nysenate.sage.model.district.DistrictMember;

public class MemberView {
    private final String name;
    private final String url;

    private MemberView(DistrictMember districtMember) {
        this.name = districtMember.memberName();
        this.url = districtMember.memberUrl();
    }

    public static MemberView from(DistrictMember districtMember) {
        if (districtMember == null) {
            return null;
        }
        return new MemberView(districtMember);
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
