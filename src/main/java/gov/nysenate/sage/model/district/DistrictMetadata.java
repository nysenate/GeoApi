package gov.nysenate.sage.model.district;

import gov.nysenate.services.model.Senator;

/**
 * Hold basic identifying information for a district such as its type, code, and member.
 */
public class DistrictMetadata {
    protected DistrictType districtType;
    protected String districtCode;
    protected String districtName;
    protected Senator senator;
    protected DistrictMember member;
    protected String link;

    public DistrictMetadata() {}

    public DistrictMetadata(DistrictType type, String name, String code) {
        if (type != null) {
            this.districtType = type;
            this.districtCode = code;
            this.districtName = name;
        }
    }

    public DistrictType getDistrictType() {
        return districtType;
    }

    public void setDistrictType(DistrictType districtType) {
        this.districtType = districtType;
    }

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = districtCode;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public Senator getSenator() {
        return senator;
    }

    public void setSenator(Senator senator) {
        this.senator = senator;
    }

    public DistrictMember getMember() {
        return member;
    }

    public void setMember(DistrictMember member) {
        this.member = member;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
