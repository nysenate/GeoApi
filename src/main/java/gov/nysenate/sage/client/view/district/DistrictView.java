package gov.nysenate.sage.client.view.district;

import com.fasterxml.jackson.annotation.JsonRawValue;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.district.SingleDistrict;
import gov.nysenate.sage.model.result.DistrictResultWithMembers;
import lombok.Getter;

public class DistrictView {
    private final SingleDistrict district;
    private final DistrictType type;
    // Raw GeoJSON geometry, emitted directly to the client.
    @Getter
    @JsonRawValue
    private final String map;

    protected DistrictView(SingleDistrict data, DistrictType type, DistrictMap map) {
        this.district = data;
        this.type = type;
        this.map = map == null ? null : map.getMapGeoJson();
    }

    public static DistrictView from(DistrictType type, DistrictResultWithMembers result, DistrictMap districtMap) {
        DistrictInfo info = result.getDistrictInfo();
        SingleDistrict singleDistrict = info.getDistrict(type);
        if (singleDistrict == null) {
            return null;
        }
        return switch (type) {
            case SENATE -> new MemberDistrictView(singleDistrict, type, districtMap, result.getSenator());
            case ASSEMBLY -> new MemberDistrictView(singleDistrict, type, districtMap, result.getAssemblyMember());
            case CONGRESSIONAL -> new MemberDistrictView(singleDistrict, type, districtMap, result.getCongressionalMember());
            default -> new DistrictView(singleDistrict, type, districtMap);
        };
    }

    public String getName() {
        return district.name();
    }

    public String getDistrict() {
        return district.code();
    }

    public String getDisplayName() {
        return type.getDisplayName();
    }
}
