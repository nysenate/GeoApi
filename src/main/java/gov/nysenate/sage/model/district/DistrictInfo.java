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
    /** A set of the DistrictType's that were actually district assigned. */
    protected Set<DistrictType> assignedDistricts = new LinkedHashSet<>();

    protected Map<DistrictType, String> districtNames = new HashMap<>();
    protected Map<DistrictType, String> districtCodes = new HashMap<>();
    protected Map<DistrictType, DistrictMap> districtMaps = new HashMap<>();

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

    public Set<DistrictType> getAssignedDistricts() {
        return assignedDistricts;
    }

    public Map<DistrictType, String> getDistrictCodes() {
        return this.districtCodes;
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

    public boolean hasValidDistCode(DistrictType districtType)
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
