package gov.nysenate.sage.model.district;

import java.util.HashMap;

/**
 * The district shapefiles each have their own table structure with different
 * column names that represent the district name and code. This class serves
 * as a reference lookup to obtain the column names on a DistrictType basis.
 */
public enum DistrictShapeCode
{
    INSTANCE;

    private static HashMap<DistrictType, String> nameColumn = new java.util.HashMap<>();
    private static HashMap<DistrictType, String> codeColumn = new HashMap<>();
    private static HashMap<DistrictType, String> sridColumn = new HashMap<>();

    static {
        nameColumn.put(DistrictType.SENATE, "NAMELSAD");
        nameColumn.put(DistrictType.CONGRESSIONAL, "NAME");
        nameColumn.put(DistrictType.ASSEMBLY, "NAME");
        nameColumn.put(DistrictType.COUNTY, "NAMELSAD");
        nameColumn.put(DistrictType.SCHOOL, "EANAME1");
        nameColumn.put(DistrictType.TOWN, "NAME");
        nameColumn.put(DistrictType.ZIP, "ZCTA5CE10");

        codeColumn.put(DistrictType.SENATE, "SD_CODE");
        codeColumn.put(DistrictType.CONGRESSIONAL, "DISTRICT");
        codeColumn.put(DistrictType.ASSEMBLY, "DISTRICT");
        codeColumn.put(DistrictType.COUNTY, "COUNTYFP");
        codeColumn.put(DistrictType.SCHOOL, "TFCODE");
        codeColumn.put(DistrictType.TOWN, "ABBREV");
        codeColumn.put(DistrictType.ZIP, "ZCTA5CE10");

        sridColumn.put(DistrictType.SENATE, "4269");
        sridColumn.put(DistrictType.CONGRESSIONAL, "4326");
        sridColumn.put(DistrictType.ASSEMBLY, "4326");
        sridColumn.put(DistrictType.COUNTY, "4326");
        sridColumn.put(DistrictType.SCHOOL, "4326");
        sridColumn.put(DistrictType.TOWN, "4326");
        sridColumn.put(DistrictType.ZIP, "4269");
    }

    public static boolean contains(DistrictType districtType)
    {
        return codeColumn.containsKey(districtType);
    }

    public static String getNameColumn(DistrictType districtType)
    {
        return nameColumn.get(districtType);
    }

    public static String getCodeColumn(DistrictType districtType)
    {
        return codeColumn.get(districtType);
    }

    public static String getSridColumn(DistrictType districtType)
    {
        return sridColumn.get(districtType);
    }
}
