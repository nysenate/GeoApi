package gov.nysenate.sage.model.district;

import gov.nysenate.sage.model.geo.Polygon;
import gov.nysenate.services.model.Senator;

import java.math.BigDecimal;
import java.util.*;

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

    /** A map of `targetType` district codes along with the area of intersection in `areaUnit`s */
    protected Map<String, BigDecimal> targetOverlap = new HashMap<>();

    /** A map of `targetType` district codes along with the intersection geometry */
    protected Map<String, DistrictMap> intersectionMaps = new HashMap<>();

    /** Associates target district code to district maps */
    protected Map<String, DistrictMap> targetDistrictMaps = new HashMap<>();

    /** Associates district members by target district code if applicable */
    protected Map<String, DistrictMember> targetDistrictMembers = new HashMap<>();

    /** Associates senators by target district code if applicable */
    protected Map<String, Senator> targetSenators = new HashMap<>();

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

    public Set<String> getTargetDistricts() {
        return this.targetOverlap.keySet();
    }

    public Map<String, DistrictMap> getTargetDistrictMaps() {
        return targetDistrictMaps;
    }

    public void setTargetDistrictMaps(Map<String, DistrictMap> targetDistrictMaps) {
        this.targetDistrictMaps = targetDistrictMaps;
    }

    /**
     * Retrieve district maps associated with the overlaps.
     * @param district The code as in '46' for senate district 46.
     * @return DistrictMap
     */
    public DistrictMap getTargetDistrictMap(String district) {
        return this.targetDistrictMaps.get(district);
    }

    public void setTargetDistrictMap(String district, DistrictMap districtMap) {
        this.targetDistrictMaps.put(district, districtMap);
    }

    public Map<String, BigDecimal> getTargetOverlap() {
        return targetOverlap;
    }

    public void setTargetOverlap(Map<String, BigDecimal> targetOverlap) {
        this.targetOverlap = targetOverlap;
    }

    public BigDecimal getTargetOverlap(String district) {
        return this.targetOverlap.get(district);
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

    public Map<String, Senator> getTargetSenators() {
        return targetSenators;
    }

    public void setTargetSenators(Map<String, Senator> targetSenators) {
        this.targetSenators = targetSenators;
    }

    public Map<String, DistrictMember> getTargetDistrictMembers() {
        return targetDistrictMembers;
    }

    public void setTargetDistrictMembers(Map<String, DistrictMember> targetDistrictMembers) {
        this.targetDistrictMembers = targetDistrictMembers;
    }

    public DistrictMember getTargetDistrictMember(String district) {
        return this.targetDistrictMembers.get(district);
    }

    public Map<String, DistrictMap> getIntersectionMaps() {
        return intersectionMaps;
    }

    public void setIntersectionMaps(Map<String, DistrictMap> intersectionMaps) {
        this.intersectionMaps = intersectionMaps;
    }

    public DistrictMap getIntersectionMap(String district) {
        return this.intersectionMaps.get(district);
    }

    public void addIntersectionMap(String district, DistrictMap intersectionMap) {
        this.intersectionMaps.put(district, intersectionMap);
    }
}