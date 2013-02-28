package gov.nysenate.sage.model.district;

public class Assembly
{
    private int district;
    private String memberName;
    private String memberUrl;

    public Assembly() {}

    public Assembly(int district, String memberName, String memberUrl)
    {
        this.district = district;
        this.memberName = memberName;
        this.memberUrl = memberUrl;
    }

    public int getDistrict() {
        return district;
    }

    public void setDistrict(int district) {
        this.district = district;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberUrl() {
        return memberUrl;
    }

    public void setMemberUrl(String memberUrl) {
        this.memberUrl = memberUrl;
    }
}
