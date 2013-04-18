package gov.nysenate.sage.model.district;

import gov.nysenate.services.model.Senator;

import java.util.*;

import static gov.nysenate.sage.model.district.DistrictType.*;

/**
 * DistrictInfo is used as a container for all assigned district names, codes, and district maps.
 * It is designed to allow for quick look ups using maps as opposed to iterating over lists of districts.
 */
public class DistrictInfo
{
    protected Senator senator;

    /** A set of DistrictTypes that were actually district assigned. */
    protected Set<DistrictType> assignedDistricts = new LinkedHashSet<>();

    /** A set of the DistrictTypes that might be incorrectly assigned. */
    protected Set<DistrictType> uncertainDistricts = new LinkedHashSet<>();

    /** District names and codes */
    protected Map<DistrictType, String> districtNames = new HashMap<>();
    protected Map<DistrictType, String> districtCodes = new HashMap<>();

    /** Names of assembly/congressional members */
    protected Map<DistrictType, DistrictMember> districtMembers = new HashMap<>();

    /** District Maps and proximity values */
    protected Map<DistrictType, DistrictMap> districtMaps = new HashMap<>();
    protected Map<DistrictType, Double> districtProximities = new HashMap<>();

    /** Neighboring District Maps */
    protected Map<DistrictType, List<DistrictMap>> neighborMaps = new HashMap<>();

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

    public String getDistName(DistrictType districtType)
    {
        return this.districtNames.get(districtType);
    }

    public void setDistName(DistrictType districtType, String name)
    {
        this.districtNames.put(districtType, name);
    }

    public String getDistCode(DistrictType districtType)
    {
        return this.districtCodes.get(districtType);
    }

    public void setDistCode(DistrictType districtType, String code)
    {
        this.districtCodes.put(districtType, code);
        if (isValidDistCode(code)) {
            this.assignedDistricts.add(districtType);
        }
        else {
            this.assignedDistricts.remove(districtType);
        }
    }

    public DistrictMap getDistMap(DistrictType districtType)
    {
        return this.districtMaps.get(districtType);
    }

    public void setDistMap(DistrictType districtType, DistrictMap districtMap)
    {
        this.districtMaps.put(districtType, districtMap);
    }

    public DistrictMember getDistrictMember(DistrictType districtType)
    {
        return this.districtMembers.get(districtType);
    }

    public void setDistrictMember(DistrictType districtType, DistrictMember districtMember)
    {
        this.districtMembers.put(districtType, districtMember);
    }

    public Double getDistProximity(DistrictType districtType)
    {
        return districtProximities.get(districtType);
    }

    public void setDistProximity(DistrictType districtType, Double districtProximity)
    {
        this.districtProximities.put(districtType, districtProximity);
    }

    public Set<DistrictType> getUncertainDistricts() {
        return uncertainDistricts;
    }

    public void setUncertainDistricts(Set<DistrictType> uncertainDistricts) {
        this.uncertainDistricts = uncertainDistricts;
    }

    public void addUncertainDistrict(DistrictType districtType) {
        this.uncertainDistricts.add(districtType);
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

    public void addNeighborMaps(DistrictType districtType, List<DistrictMap> neighborMaps) {
        this.neighborMaps.put(districtType, neighborMaps);
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

    public boolean hasDistrictCode(DistrictType districtType)
    {
        return isValidDistCode(this.districtCodes.get(districtType));
    }

    private boolean isValidDistCode(String code){
        if (code != null) {
            String c = code.trim().replaceFirst("^0+(?!$)", "");
            if ( !c.isEmpty() && !c.equalsIgnoreCase("null") && !c.equals("0") && !c.equals("000")) {
                return true;
            }
        }
        return false;
    }
}
