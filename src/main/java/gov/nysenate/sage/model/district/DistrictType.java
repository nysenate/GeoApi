package gov.nysenate.sage.model.district;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum DistrictType
{
    ASSEMBLY, CONGRESSIONAL, COUNTY, SENATE, SCHOOL, TOWN, ZIP,  // Available as shape files
    ELECTION, WARD, CLEG, CITY, FIRE, VILLAGE, CITY_COUNCIL;     // Available only in street files

    /** A Map container is used to associate type names with the enum type */
    private static final Map<String, DistrictType> resolveMap = new HashMap<>();
    static {
        for (DistrictType dt : DistrictType.getAllTypes()){
            resolveMap.put(dt.name().toUpperCase(), dt);
        }
    }

    public static List<DistrictType> getStandardTypes()
    {
        return Arrays.asList(ASSEMBLY, CONGRESSIONAL, COUNTY, SENATE, SCHOOL, TOWN, ZIP);
    }

    public static List<DistrictType> getExtendedTypes()
    {
        return Arrays.asList(ELECTION, WARD, CLEG, CITY, FIRE, VILLAGE, CITY_COUNCIL);
    }

    public static List<DistrictType> getStateBasedTypes()
    {
        return Arrays.asList(SENATE, CONGRESSIONAL, ASSEMBLY, COUNTY);
    }

    public static List<DistrictType> getCountyBasedTypes()
    {
        return Arrays.asList(SCHOOL, TOWN, CLEG);
    }

    public static List<DistrictType> getTownBasedTypes()
    {
        return Arrays.asList(WARD, ELECTION);
    }

    public static List<DistrictType> getAllTypes()
    {
        return Arrays.asList(DistrictType.values());
    }

    /** Returns the DistrictType that matches the string representation */
    public static DistrictType resolveType(String type)
    {
        return (type == null? null : resolveMap.get(type.toUpperCase()));
    }
}
