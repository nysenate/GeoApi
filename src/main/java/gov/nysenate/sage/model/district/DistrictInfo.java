package gov.nysenate.sage.model.district;

import gov.nysenate.sage.model.address.DistrictedStreetRange;

import java.util.*;

import static gov.nysenate.sage.controller.api.DistrictUtil.isValidDistCode;
import static gov.nysenate.sage.model.district.DistrictType.*;

/**
 * DistrictInfo is used as a container for all assigned district names, codes, and district maps.
 * It is designed to allow for quick lookups using maps as opposed to iterating over lists of districts.
 */
public class DistrictInfo {
    /** A set of DistrictTypes that were actually district assigned. */
    private final Set<DistrictType> assignedDistricts = new LinkedHashSet<>();

    /** District names and codes */
    private final Map<DistrictType, String> districtNames = new HashMap<>();
    private final Map<DistrictType, String> districtCodes = new HashMap<>();
    private final Map<DistrictType, DistrictOverlap> districtOverlaps = new HashMap<>();

    private List<DistrictedStreetRange> streetRanges = new ArrayList<>();
    private DistrictMatchLevel matchLevel = DistrictMatchLevel.NOMATCH;

    public DistrictInfo() {}

    public DistrictInfo(String congressionalCode, String countyCode, String senateCode,
                        String assemblyCode, String townCode, String schoolCode) {
        this.setDistCode(CONGRESSIONAL, congressionalCode);
        this.setDistCode(COUNTY, countyCode);
        this.setDistCode(SENATE, senateCode);
        this.setDistCode(ASSEMBLY, assemblyCode);
        this.setDistCode(TOWN_CITY, townCode);
        this.setDistCode(SCHOOL, schoolCode);
    }

    public DistrictMatchLevel getMatchLevel() {
        return matchLevel;
    }

    public void setMatchLevel(DistrictMatchLevel matchLevel) {
        this.matchLevel = matchLevel;
    }

    public String getDistName(DistrictType districtType) {
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
     * this method. Also, if it's a senate, congressional, or assembly district, a default name is set for it.
     */
    public void setDistCode(DistrictType districtType, String code) {
        districtCodes.put(districtType, code);
        if (isValidDistCode(code)) {
            assignedDistricts.add(districtType);

            String name = districtType.getNameFromCode(code);
            if (name != null) {
                districtNames.put(districtType, name);
            }
        }
        else {
            assignedDistricts.remove(districtType);
        }
    }

    public Set<DistrictType> getAssignedDistricts() {
        return assignedDistricts;
    }

    /**
    * Multi Districts
    */
    public Map<DistrictType, DistrictOverlap> getDistrictOverlaps() {
        return districtOverlaps;
    }


    public List<DistrictedStreetRange> getStreetRanges() {
        return streetRanges;
    }

    public void setStreetRanges(List<DistrictedStreetRange> streetRanges) {
        this.streetRanges = streetRanges;
    }

    @Override
    public String toString() {
        var out = new StringBuilder();
        for (DistrictType t : assignedDistricts) {
            out.append(t).append(": name = ").append(getDistName(t)).append(" code = ").append(getDistCode(t)).append("\n");
        }
        return out.toString();
    }
}
