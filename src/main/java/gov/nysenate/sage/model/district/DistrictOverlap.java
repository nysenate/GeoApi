package gov.nysenate.sage.model.district;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a mapping of districts that overlap a given reference area.
 */
// TODO: don't really need
public class DistrictOverlap {
    /** A map of `targetType` district codes along with the intersection geometry */
    private final Map<String, DistrictMap> intersectionMaps = new HashMap<>();

    public String getAreaUnit() {
        return "SQ_METERS";
    }

    public BigDecimal getTotalArea() {
        return intersectionMaps.values().stream().map(DistrictMap::getArea).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addIntersectionMap(String district, DistrictMap intersectionMap) {
        intersectionMaps.put(district, intersectionMap);
    }

    public Map<String, DistrictMap> getIntersectionMaps() {
        return intersectionMaps;
    }
}