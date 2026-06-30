package gov.nysenate.sage.model.district;

import gov.nysenate.sage.model.geo.Polygon;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class IntersectMap extends DistrictMap {
    private List<Polygon> fullMapPolygons;

    public IntersectMap(DistrictType type, SingleDistrict districtData) {
        super(type, districtData.name(), districtData.code());
    }
}
