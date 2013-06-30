package gov.nysenate.sage.model.district;

import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.services.model.Senator;

import java.math.BigDecimal;
import java.util.*;

import static gov.nysenate.sage.model.district.DistrictType.*;

/**
 * DistrictInfo is used as a container for all assigned district names, codes, and district maps.
 * It is designed to allow for quick look ups using maps as opposed to iterating over lists of districts.
 */
public class DistrictInfo
{
    /** A set of DistrictTypes that were actually district assigned. */
    protected Set<DistrictType> assignedDistricts = new LinkedHashSet<>();

    /** A set of the DistrictTypes that might be incorrectly assigned. */
    protected Set<DistrictType> nearBorderDistricts = new LinkedHashSet<>();

    /** District names and codes */
    protected Map<DistrictType, String> districtNames = new HashMap<>();
    protected Map<DistrictType, String> districtCodes = new HashMap<>();

    /** Names of senate/assembly/congressional members */
    protected Senator senator;
    protected Map<DistrictType, DistrictMember> districtMembers = new HashMap<>();

    /** District Maps and proximity values */
    protected Map<DistrictType, DistrictMap> districtMaps = new HashMap<>();
    protected Map<DistrictType, Double> districtProximities = new HashMap<>();

    /** Neighboring District Maps */
    protected Map<DistrictType, List<DistrictMap>> neighborMaps = new HashMap<>();

    /** Multi District Overlap/Map data */
    protected DistrictMap referenceMap;
    protected Map<DistrictType, DistrictOverlap> districtOverlaps = new HashMap<>();

    public DistrictInfo() {}

    public DistrictInfo(String congressionalCode, String countyCode, String senateCode,
                        String assemblyCode, String townCode, String schoolCode)
    {
        this.setDistCode(CONGRESSIONAL, congressionalCode);
        this.setDistCode(COUNTY, countyCode);
        this.setDistCode(SENATE, senateCode);
        this.setDistCode(ASSEMBLY, assemblyCode);
        this.setDistCode(TOWN, townCode);
        this.setDistCode(SCHOOL, schoolCode);
    }

    public Senator getSenator() {
        return senator;
    }

    public void setSenator(Senator senator) {
        this.senator = senator;
    }

    public String getDistName(DistrictType districtType){
        return this.districtNames.get(districtType);
    }

    public void setDistName(DistrictType districtType, String name) {
        this.districtNames.put(districtType, name);
    }

    public String getDistCode(DistrictType districtType) {
        return this.districtCodes.get(districtType);
    }

    /**
     * Sets a district code for a given type. A district is marked as assigned if the code is set through
     * this method. Also if its a senate, congressional, or assembly district, a default name is set for it.
     * @param districtType
     * @param code
     */
    public void setDistCode(DistrictType districtType, String code)
    {
        this.districtCodes.put(districtType, code);
        if (isValidDistCode(code)) {
            this.assignedDistricts.add(districtType);

            /** Fill in the names for congressional and assembly districts */
            if (districtType.equals(SENATE)) {
                this.districtNames.put(SENATE, "NY Senate District " + code);
            }
            else if (districtType.equals(CONGRESSIONAL)) {
                this.districtNames.put(CONGRESSIONAL, "NY Congressional District " + code);
            }
            else if (districtType.equals(ASSEMBLY)) {
                this.districtNames.put(ASSEMBLY, "NY Assembly District " + code);
            }
        }
        else {
            this.assignedDistricts.remove(districtType);
        }
    }

    public DistrictMap getDistMap(DistrictType districtType) {
        return this.districtMaps.get(districtType);
    }

    public void setDistMap(DistrictType districtType, DistrictMap districtMap) {
        this.districtMaps.put(districtType, districtMap);
    }

    public DistrictMember getDistrictMember(DistrictType districtType) {
        return this.districtMembers.get(districtType);
    }

    public void setDistrictMember(DistrictType districtType, DistrictMember districtMember) {
        this.districtMembers.put(districtType, districtMember);
    }

    public Double getDistProximity(DistrictType districtType) {
        return districtProximities.get(districtType);
    }

    public void setDistProximity(DistrictType districtType, Double districtProximity) {
        this.districtProximities.put(districtType, districtProximity);
    }

    public Set<DistrictType> getNearBorderDistricts() {
        return nearBorderDistricts;
    }

    public void setNearBorderDistricts(Set<DistrictType> nearBorderDistricts) {
        this.nearBorderDistricts = nearBorderDistricts;
    }

    public void addNearBorderDistrict(DistrictType districtType) {
        this.nearBorderDistricts.add(districtType);
    }

    public Set<DistrictType> getAssignedDistricts() {
        return assignedDistricts;
    }

    public void setAssignedDistricts(Set<DistrictType> assignedDistricts) {
        this.assignedDistricts = assignedDistricts;
    }

    public Map<DistrictType, String> getDistrictCodes() {
        return this.districtCodes;
    }

    public Map<DistrictType, DistrictMap> getDistrictMaps() {
        return this.districtMaps;
    }

    public Map<DistrictType, List<DistrictMap>> getNeighborMaps() {
        return neighborMaps;
    }

    /**
     * Get neighboring DistrictMaps if they exist or an empty List.
     * @param districtType  The district type to get neighbor maps for.
     * @return List<DistrictMap>
     */
    public List<DistrictMap> getNeighborMaps(DistrictType districtType) {
        return (neighborMaps.get(districtType) != null) ? neighborMaps.get(districtType) : new ArrayList<DistrictMap>();
    }

    public void setNeighborMaps(Map<DistrictType, List<DistrictMap>> neighborMaps) {
        this.neighborMaps = neighborMaps;
    }

    /**
     * Links a list of district maps (which represent the neighbor districts) to a district type.
     * @param districtType
     * @param neighborMaps
     */
    public void addNeighborMaps(DistrictType districtType, List<DistrictMap> neighborMaps) {
        this.neighborMaps.put(districtType, neighborMaps);
    }

    /**
    * Multi Districts
    */
    public Map<DistrictType, DistrictOverlap> getDistrictOverlaps() {
        return districtOverlaps;
    }

    public void setDistrictOverlaps(Map<DistrictType, DistrictOverlap> districtOverlaps) {
        this.districtOverlaps = districtOverlaps;
    }

    public void addDistrictOverlap(DistrictType districtType, DistrictOverlap districtOverlap)
    {
        this.districtOverlaps.put(districtType, districtOverlap);
    }

    public DistrictOverlap getDistrictOverlap(DistrictType districtType)
    {
        return this.districtOverlaps.get(districtType);
    }

    public DistrictMap getReferenceMap() {
        return referenceMap;
    }

    public void setReferenceMap(DistrictMap referenceMap) {
        this.referenceMap = referenceMap;
    }

    /**
     * Checks the district code map to see if a valid code exists for the given districtType.
     * @param districtType
     * @return true if it has a valid code, false otherwise
     */
    public boolean hasDistrictCode(DistrictType districtType)
    {
        return isValidDistCode(this.districtCodes.get(districtType));
    }

    /**
     * Determines if code is valid or not by ensuring that the trimmed code does not equal '', 0, or null.
     * @param code
     * @return
     */
    private boolean isValidDistCode(String code){
        if (code != null) {
            String c = FormatUtil.trimLeadingZeroes(code.trim());
            if ( !c.isEmpty() && !c.equalsIgnoreCase("null") && !c.equals("0") && !c.equals("000")) {
                return true;
            }
        }
        return false;
    }

    public String toString()
    {
        String out = "";
        for (DistrictType t : assignedDistricts){
            out += t + ": name = " + getDistName(t)
                    +  " code = " + getDistCode(t) + " map = " + getDistMap(t) + "\n";
        }
        return out;
    }
}
