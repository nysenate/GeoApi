package gov.nysenate.sage.dao.provider.district;

import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.district.IntersectMap;
import gov.nysenate.sage.model.geo.Geocode;

import java.util.List;
import java.util.Set;

public interface ShapefileDao {
    /**
     * Retrieves a DistrictInfo object based on the districts that intersect the given point.
     * @param geocode        Geocode of interest
     * @param districtTypes  Collection of district types to resolve
     * @return  DistrictInfo if query was successful, null otherwise
     */
    DistrictInfo getDistrictInfo(Geocode geocode, Set<DistrictType> districtTypes);

    /**
     * Creates and returns a DistrictOverlap object which contains lists of all districts that contained
     * within a collection of other districts and maps of intersections for senate districts. This is used
     * for a zip/city level match where given a collection of zip codes, gather the other types of districts
     * that overlap the zip area.
     * @param targetDistrictType The DistrictType of get overlap info for.
     * @param refDistrictType    The DistrictType to base the intersections of off.
     * @param refCode           The code that represents the base area.
     */
    List<IntersectMap> getDistrictOverlap(DistrictType targetDistrictType,
                                          DistrictType refDistrictType, String refCode);

    /**
     * Retrieves a mapped collection of DistrictMaps.
     * @return Map<DistrictType, List<DistrictMap>>
     */
    List<DistrictMap> getDistrictMaps(DistrictType type);

    /**
     * Fetches all the district maps from the database and stores them in a collection as well as
     * a lookup cache for fast retrieval.
     */
    void cacheDistrictMaps();

    DistrictMap getDistrictMap(DistrictType type, String code);

    String getDistrictName(DistrictType type, String code);
}
