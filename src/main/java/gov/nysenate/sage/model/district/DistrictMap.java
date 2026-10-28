package gov.nysenate.sage.model.district;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Contains district map geometry information.
 */
@Getter
public class DistrictMap {
    private final DistrictType districtType;
    private final DistrictId id;
    // Raw GeoJSON geometry (a MultiPolygon).
    @Setter
    private String mapGeoJson;
    // Note that this is only an approximation.
    @Setter
    private BigDecimal area;

    public DistrictMap(DistrictType type, DistrictId id) {
        this.districtType = type;
        this.id = id;
    }

    @Override
    public String toString() {
        return mapGeoJson == null ? "" : mapGeoJson;
    }
}
