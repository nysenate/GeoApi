package gov.nysenate.sage.model.district;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a mapping of districts that overlap a given reference area.
 */
public class DistrictOverlap {
    /** A map of `targetType` district codes along with the intersection geometry */
    private final Map<String, DistrictMap> intersectionMaps = new HashMap<>();

    public DistrictMap getDistrictMap(String district) {
        return intersectionMaps.get(district);
    }

    public String getAreaUnit() {
        return "SQ_METERS";
    }

    public BigDecimal getTotalArea() {
        return intersectionMaps.values().stream().map(DistrictMap::getArea).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public DistrictMap getIntersectionMap(String district) {
        return intersectionMaps.get(district);
    }

    public void addIntersectionMap(String district, DistrictMap intersectionMap) {
        intersectionMaps.put(district, intersectionMap);
    }
}