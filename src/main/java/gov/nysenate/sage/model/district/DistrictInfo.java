package gov.nysenate.sage.model.district;

import gov.nysenate.sage.model.address.DistrictedStreetRange;
import gov.nysenate.sage.model.geo.Line;
import gov.nysenate.services.model.Senator;
import org.apache.commons.lang.WordUtils;

import java.util.*;

import static gov.nysenate.sage.controller.api.DistrictUtil.isValidDistCode;
import static gov.nysenate.sage.model.district.DistrictType.*;

/**
 * DistrictInfo is used as a container for all assigned district names, codes, and district maps.
 * It is designed to allow for quick look ups using maps as opposed to iterating over lists of districts.
 */
public class DistrictInfo
{
    /** A set of DistrictTypes that were actually district assigned. */
    protected Set<DistrictType> assignedDistricts = new LinkedHashSet<>();

    /** If true, then all districts that were requested were assigned */
    protected boolean completelyAssigned;

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
    protected List<Line> streetLineReference;
    protected Map<DistrictType, DistrictOverlap> districtOverlaps = new HashMap<>();
    protected List<DistrictedStreetRange> streetRanges = new ArrayList<>();

    public DistrictInfo() {}

    public DistrictInfo(String congressionalCode, String countyCode, String senateCode,
                        String assemblyCode, String townCode, String schoolCode)
    {
        this.setDistCode(CONGRESSIONAL, congressionalCode);
        this.setDistCode(COUNTY, countyCode);
        this.setDistCode(SENATE, senateCode);
        this.setDistCode(ASSEMBLY, assemblyCode);
        this.setDistCode(TOWN_CITY, townCode);
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
        return districtCodes.get(districtType);
    }

    /**
     * Sets a district code for a given type. A district is marked as assigned if the code is set through
     * this method. Also if its a senate, congressional, or assembly district, a default name is set for it.
     * @param districtType
     * @param code
     */
    public void setDistCode(DistrictType districtType, String code) {
        districtCodes.put(districtType, code);
        if (isValidDistCode(code)) {
            assignedDistricts.add(districtType);

            /** Fill in the names for congressional and assembly districts */
            if (districtType == SENATE || districtType == ASSEMBLY || districtType == CONGRESSIONAL) {
                String displayStr = WordUtils.capitalizeFully(districtType.name());
                districtNames.put(districtType, "NY %s District %s".formatted(displayStr, code));
            }
        }
        else {
            assignedDistricts.remove(districtType);
        }
    }

    public void setCompletelyAssigned(boolean completelyAssigned) {
        this.completelyAssigned = completelyAssigned;
    }

    public DistrictMap getDistMap(DistrictType districtType) {
        return districtMaps.get(districtType);
    }

    public void setDistMap(DistrictType districtType, DistrictMap districtMap) {
        districtMaps.put(districtType, districtMap);
    }

    public DistrictMember getDistrictMember(DistrictType districtType) {
        return districtMembers.get(districtType);
    }

    public void setDistrictMember(DistrictType districtType, DistrictMember districtMember) {
        districtMembers.put(districtType, districtMember);
    }

    public Double getDistProximity(DistrictType districtType) {
        return districtProximities.get(districtType);
    }

    public void setDistProximity(DistrictType districtType, Double districtProximity) {
        districtProximities.put(districtType, districtProximity);
    }

    public Set<DistrictType> getNearBorderDistricts() {
        return nearBorderDistricts;
    }

    public void addNearBorderDistrict(DistrictType districtType) {
        nearBorderDistricts.add(districtType);
    }

    public Set<DistrictType> getAssignedDistricts() {
        return assignedDistricts;
    }

    public Map<DistrictType, String> getDistrictCodes() {
        return districtCodes;
    }

    /**
     * Get neighboring DistrictMaps if they exist or an empty List.
     * @param districtType  The district type to get neighbor maps for.
     * @return List<DistrictMap>
     */
    public List<DistrictMap> getNeighborMaps(DistrictType districtType) {
        return (neighborMaps.get(districtType) != null) ? neighborMaps.get(districtType) : new ArrayList<>();
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

    public void addDistrictOverlap(DistrictType districtType, DistrictOverlap districtOverlap) {
        districtOverlaps.put(districtType, districtOverlap);
    }

    public DistrictOverlap getDistrictOverlap(DistrictType districtType) {
        return districtOverlaps.get(districtType);
    }

    public DistrictMap getReferenceMap() {
        return referenceMap;
    }

    public void setReferenceMap(DistrictMap referenceMap) {
        this.referenceMap = referenceMap;
    }

    public List<Line> getStreetLineReference() {
        return streetLineReference;
    }

    public void setStreetLineReference(List<Line> streetLineReference) {
        this.streetLineReference = streetLineReference;
    }

    public List<DistrictedStreetRange> getStreetRanges() {
        return streetRanges;
    }

    public void setStreetRanges(List<DistrictedStreetRange> streetRanges) {
        this.streetRanges = streetRanges;
    }

    /**
     * Checks the district code map to see if a valid code exists for the given districtType.
     * @param districtType
     * @return true if it has a valid code, false otherwise
     */
    public boolean hasDistrictCode(DistrictType districtType) {
        return isValidDistCode(this.districtCodes.get(districtType));
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (DistrictType t : assignedDistricts){
            out.append(t).append(": name = ").append(getDistName(t)).append(" code = ").append(getDistCode(t)).append(" map = ").append(getDistMap(t)).append("\n");
        }
        return out.toString();
    }
}
