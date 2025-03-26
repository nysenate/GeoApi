package gov.nysenate.sage.model.district;

import java.util.Objects;

/**
 * Represents a generic member associated with a district number
 */
public class DistrictMember {
    private DistrictType districtType;
    private int district;
    private String memberName;
    private String memberUrl;

    public DistrictMember() {}

    public DistrictMember(int district, String memberName, String memberUrl) {
        this.district = district;
        this.memberName = memberName;
        this.memberUrl = memberUrl;
    }

    public DistrictMember(DistrictType districtType, int district, String memberName, String memberUrl) {
        this.districtType = districtType;
        this.district = district;
        this.memberName = memberName;
        this.memberUrl = memberUrl;
    }

    public DistrictType getDistrictType() {
        return districtType;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DistrictMember that = (DistrictMember) o;
        return district == that.district && districtType == that.districtType &&
                Objects.equals(memberName, that.memberName) && Objects.equals(memberUrl, that.memberUrl);
    }
}
