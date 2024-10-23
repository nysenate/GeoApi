package gov.nysenate.sage.model.district;

import gov.nysenate.services.model.Senator;

import java.math.BigDecimal;
import java.util.*;

/**
 * Represents a mapping of districts that overlap a given reference area. Typically, this is used
 * to determine which senate districts overlap a set of zipcode regions. However, any form of overlap
 * amongst district types can be represented here.
 */
public class DistrictOverlap {
    /** A map of `targetType` district codes along with the area of intersection in `areaUnit`s */
    private final Map<String, BigDecimal> targetOverlap = new HashMap<>();

    /** A map of `targetType` district codes along with the intersection geometry */
    private final Map<String, DistrictMap> intersectionMaps = new HashMap<>();

    /** Associates target district code to district maps */
    private final Map<String, DistrictMap> targetDistrictMaps = new HashMap<>();

    /** Associates senators by target district code if applicable */
    private Map<String, Senator> targetSenators = new HashMap<>();

    /** The number of `areaUnits` that encompass the union of the areas of all the reference codes */
    protected BigDecimal totalArea;

    public Set<String> getTargetDistricts() {
        return this.targetOverlap.keySet();
    }

    /**
     * Retrieve district maps associated with the overlaps.
     * @param district The code as in '46' for senate district 46.
     * @return DistrictMap
     */
    public DistrictMap getTargetDistrictMap(String district) {
        return targetDistrictMaps.get(district);
    }

    public void setTargetDistrictMap(String district, DistrictMap districtMap) {
        targetDistrictMaps.put(district, districtMap);
    }

    public Map<String, BigDecimal> getTargetOverlap() {
        return targetOverlap;
    }

    public BigDecimal getTargetOverlap(String district) {
        return targetOverlap.get(district);
    }

    public List<String> getOverlapDistrictCodes() {
        return new ArrayList<>(this.targetOverlap.keySet());
    }

    public String getAreaUnit() {
        return "SQ_METERS";
    }

    public BigDecimal getTotalArea() {
        return totalArea;
    }

    public void setTotalArea(BigDecimal totalArea) {
        this.totalArea = totalArea;
    }

    public Map<String, Senator> getTargetSenators() {
        return targetSenators;
    }

    public void setTargetSenators(Map<String, Senator> targetSenators) {
        this.targetSenators = targetSenators;
    }

    public DistrictMap getIntersectionMap(String district) {
        return intersectionMaps.get(district);
    }

    public void addIntersectionMap(String district, DistrictMap intersectionMap) {
        intersectionMaps.put(district, intersectionMap);
    }
}