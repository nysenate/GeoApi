package gov.nysenate.sage.model.district;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static gov.nysenate.sage.model.district.DistrictType.*;

/**
 * DistrictInfo is used as a container for all assigned district names, codes, and district maps.
 */
public class DistrictInfo
{
    /** A set of the DistrictType's that were actually district assigned.
     *  Whenever a district is set using the constructor or setter methods,
     *  the district type will be added to this assigned districts set */
    protected Set<DistrictType> assignedDistricts = new LinkedHashSet<>();

    protected Map<DistrictType, String> districtNames = new HashMap<>();
    protected Map<DistrictType, String> districtCodes = new HashMap<>();
    protected Map<DistrictType, DistrictMap> districtMaps = new HashMap<>();

    public DistrictInfo() {}

    public DistrictInfo(String congressionalCode, String countyCode, String senateCode,
                        String assemblyCode, String townCode, String schoolCode)
    {
        this.setDistrictCode(CONGRESSIONAL, congressionalCode);
        this.setDistrictCode(COUNTY, countyCode);
        this.setDistrictCode(SENATE, senateCode);
        this.setDistrictCode(ASSEMBLY, assemblyCode);
        this.setDistrictCode(TOWN, townCode);
        this.setDistrictCode(SCHOOL, schoolCode);
    }

    public String getDistrictName(DistrictType districtType)
    {
        return this.districtNames.get(districtType);
    }

    public void setDistrictName(DistrictType districtType, String name)
    {
        this.districtNames.put(districtType, name);
    }

    public String getDistrictCode(DistrictType districtType)
    {
        return this.districtCodes.get(districtType);
    }

    public void setDistrictCode(DistrictType districtType, String code)
    {
        this.districtCodes.put(districtType, code);
        this.addAssignedDistrict(districtType);
    }

    public DistrictMap getDistrictMap(DistrictType districtType)
    {
        return this.districtMaps.get(districtType);
    }

    public void setDistrictMap(DistrictType districtType, DistrictMap districtMap)
    {
        this.districtMaps.put(districtType, districtMap);
    }

    public Set<DistrictType> getAssignedDistricts() {
        return assignedDistricts;
    }

    public Map<DistrictType, String> getDistrictNames()
    {
        return this.districtNames;
    }

    protected void addAssignedDistrict(DistrictType assignedDistrict) {
        this.assignedDistricts.add(assignedDistrict);
    }
}
