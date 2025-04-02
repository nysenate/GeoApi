package gov.nysenate.sage.client.view.district;

import gov.nysenate.sage.model.district.DistrictMember;

public class MemberView
{
    protected String name;
    protected String url;

    public MemberView(DistrictMember districtMember)
    {
        if (districtMember != null) {
            this.name = districtMember.memberName();
            this.url = districtMember.memberUrl();
        }
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
