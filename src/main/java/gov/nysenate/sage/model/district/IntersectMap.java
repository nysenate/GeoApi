package gov.nysenate.sage.model.district;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IntersectMap extends DistrictMap {
    // Raw GeoJSON geometry for the full (un-clipped) map of the intersecting district.
    private String fullMapGeoJson;

    public IntersectMap(DistrictType type, SingleDistrict districtData) {
        super(type, districtData.name(), districtData.code());
    }
}
