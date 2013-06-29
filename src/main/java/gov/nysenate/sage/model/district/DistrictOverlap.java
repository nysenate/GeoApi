package gov.nysenate.sage.model.district;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a mapping of districts that overlap a given reference area. Typically this is used
 * to determine which senate districts overlap a set of zipcode regions. However any form of overlap
 * amongst district types can be represented here.
 */
public class DistrictOverlap
{
    public enum AreaUnit {
        SQ_FEET, SQ_METERS, SQ_MILES, SQ_DEGREES
    }

    /** If computing which senate districts overlap a given zip, ZIP would be the reference type. */
    protected DistrictType referenceType;

    /** If computing which senate districts overlap a given zip, SENATE would be the target type. */
    protected DistrictType targetType;

    /** The list of codes that are used as the total area to compute overlap on. For example this
     * could be a list of zip codes that represent a city. */
    protected Set<String> referenceCodes = new HashSet<>();

    /** Associates target district code to district maps */
    protected Map<String, DistrictMap> targetDistrictMaps = new HashMap<>();

    /** A map of `targetType` district codes along with the area of intersection in `areaUnit`s */
    protected Map<String, BigDecimal> targetOverlap = new HashMap<>();

    /** The unit of measurement for the totalArea */
    protected AreaUnit areaUnit;

    /** The number of `areaUnits` that encompass the union of the areas of all the reference codes */
    protected BigDecimal totalArea;

    public DistrictOverlap() {}

    public DistrictOverlap(DistrictType referenceType, DistrictType targetType, Set<String> referenceCodes, AreaUnit areaUnit)
    {
        this.referenceType = referenceType;
        this.targetType = targetType;
        this.referenceCodes = referenceCodes;
        this.areaUnit = areaUnit;
    }

    public DistrictType getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(DistrictType referenceType) {
        this.referenceType = referenceType;
    }

    public DistrictType getTargetType() {
        return targetType;
    }

    public void setTargetType(DistrictType targetType) {
        this.targetType = targetType;
    }

    public Set<String> getReferenceCodes() {
        return referenceCodes;
    }

    public void setReferenceCodes(Set<String> referenceCodes) {
        this.referenceCodes = referenceCodes;
    }

    public Map<String, DistrictMap> getTargetDistrictMaps() {
        return targetDistrictMaps;
    }

    public void setTargetDistrictMaps(Map<String, DistrictMap> targetDistrictMaps) {
        this.targetDistrictMaps = targetDistrictMaps;
    }

    public Map<String, BigDecimal> getTargetOverlap() {
        return targetOverlap;
    }

    public void setTargetOverlap(Map<String, BigDecimal> targetOverlap) {
        this.targetOverlap = targetOverlap;
    }

    public AreaUnit getAreaUnit() {
        return areaUnit;
    }

    public void setAreaUnit(AreaUnit areaUnit) {
        this.areaUnit = areaUnit;
    }

    public BigDecimal getTotalArea() {
        return totalArea;
    }

    public void setTotalArea(BigDecimal totalArea) {
        this.totalArea = totalArea;
    }
}