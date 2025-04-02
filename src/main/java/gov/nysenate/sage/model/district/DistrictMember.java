package gov.nysenate.sage.model.district;

import java.util.Objects;

/**
 * Represents a generic member associated with a district number
 */
public record DistrictMember(DistrictType districtType, int district, String memberName, String memberUrl) {

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DistrictMember that = (DistrictMember) o;
        return district == that.district && districtType == that.districtType &&
                Objects.equals(memberName, that.memberName) && Objects.equals(memberUrl, that.memberUrl);
    }
}
