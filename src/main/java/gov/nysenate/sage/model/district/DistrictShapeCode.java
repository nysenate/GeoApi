package gov.nysenate.sage.model.district;

import java.util.HashMap;

/**
 * The district shapefiles each have their own table structure with different
 * column names that represent the district name and code. This class serves
 * as a reference lookup to obtain the column names on a DistrictType basis.
 *
 * Note that the SRID value may differ between shapefiles. In order to obtain the
 * srid value inspect the .prj file located in the shapefile archive. You can then
 * use an external webservice like http://prj2epsg.org/search to convert into espg
 * code. From there you can use http://spatialreference.org/ref/epsg/{your espg code}
 * and get the srid using the PostGIS output.
 * http://www.spatialreference.org/ref/epsg/4269/postgis/
 * http://www.spatialreference.org/ref/epsg/26918/postgis/
 *
 * Alternatively, you can inspect the .prj file and the spatial_ref_sys table in the
 * geoapi database and try to find a match that way.
 */
public enum DistrictShapeCode
{
    INSTANCE;

    private static HashMap<DistrictType, String> nameColumn = new java.util.HashMap<>();
    private static HashMap<DistrictType, String> codeColumn = new HashMap<>();
    private static HashMap<DistrictType, String> sridColumn = new HashMap<>();

    static {
        nameColumn.put(DistrictType.SENATE, "NAME");
        nameColumn.put(DistrictType.CONGRESSIONAL, "NAME");
        nameColumn.put(DistrictType.ASSEMBLY, "NAME");
        nameColumn.put(DistrictType.COUNTY, "NAMELSAD");
        nameColumn.put(DistrictType.SCHOOL, "NAME");
        nameColumn.put(DistrictType.TOWN, "NAME");
        nameColumn.put(DistrictType.ZIP, "zip_code");

        codeColumn.put(DistrictType.SENATE, "DISTRICT");
        codeColumn.put(DistrictType.CONGRESSIONAL, "DISTRICT");
        codeColumn.put(DistrictType.ASSEMBLY, "DISTRICT");
        codeColumn.put(DistrictType.COUNTY, "COUNTYFP");
        codeColumn.put(DistrictType.SCHOOL, "TFCODE");
        codeColumn.put(DistrictType.TOWN, "ABBREV");
        codeColumn.put(DistrictType.ZIP, "zip_code");

        sridColumn.put(DistrictType.SENATE, "4326");
        sridColumn.put(DistrictType.CONGRESSIONAL, "4326");
        sridColumn.put(DistrictType.ASSEMBLY, "4326");
        sridColumn.put(DistrictType.COUNTY, "4326");
        sridColumn.put(DistrictType.SCHOOL, "4326");
        sridColumn.put(DistrictType.TOWN, "4326");
        sridColumn.put(DistrictType.ZIP, "4326");
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
