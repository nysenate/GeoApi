package gov.nysenate.sage.model.district;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum DistrictType
{
    ASSEMBLY,CONGRESSIONAL,COUNTY,ELECTION,SENATE,SCHOOL,TOWN,    // Standard Districts
    WARD, CLEG, CITY, FIRE, VILLAGE;                              // Extended Districts

    /** A Map container is used to associate type names with the enum type */
    private static Map<String, DistrictType> resolveMap = new HashMap<>();
    static {
        for (DistrictType dt : DistrictType.getAllTypes()){
            resolveMap.put(dt.name().toUpperCase(), dt);
        }
    }

    public static List<DistrictType> getStandardTypes()
    {
        return Arrays.asList(ASSEMBLY, CONGRESSIONAL, COUNTY, ELECTION, SENATE, SCHOOL, TOWN);
    }

    public static List<DistrictType> getExtendedTypes()
    {
        return Arrays.asList(WARD, CLEG, CITY, FIRE, VILLAGE);
    }

    public static List<DistrictType> getAllTypes()
    {
        return Arrays.asList(DistrictType.values());
    }

    /** Returns the DistrictType that matches the string representation */
    public static DistrictType resolveType(String type)
    {
        return resolveMap.get(type.toUpperCase());
    }
}
