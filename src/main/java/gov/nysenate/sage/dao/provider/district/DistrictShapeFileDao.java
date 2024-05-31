package gov.nysenate.sage.dao.provider.district;

import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictOverlap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Line;
import gov.nysenate.sage.model.geo.Point;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DistrictShapeFileDao {
    /**
     * Retrieves a DistrictInfo object based on the districts that intersect the given point.
     * @param point          Point of interest
     * @param districtTypes  Collection of district types to resolve
     * @param getSpecialMaps If true then query will return DistrictMap values for districts in the retrieveMapSet
     *                       since they do not have a unique district code identifier.
     * @return  DistrictInfo if query was successful, null otherwise
     */
    DistrictInfo getDistrictInfo(Point point, List<DistrictType> districtTypes, boolean getSpecialMaps, boolean getProximity);

    /**
     * Creates and returns a DistrictOverlap object which contains lists of all districts that contained
     * within a collection of other districts and maps of intersections for senate districts. This is used
     * for a zip/city level match where given a collection of zip codes, gather the other types of districts
     * that overlap the zip area.
     * @param targetDistrictType The DistrictType of get overlap info for.
     * @param targetCodes        The list of codes that overlap the area (obtained through street files for performance)
     * @param refDistrictType    The DistrictType to base the intersections of off.
     * @param refCodes           The list of codes that represent the base area.
     * @return DistrictOverlap
     */
    DistrictOverlap getDistrictOverlap(DistrictType targetDistrictType, Set<String> targetCodes,
                                       DistrictType refDistrictType, Set<String> refCodes);

    Map<String, List<Line>> getIntersectingStreetLine(DistrictType districtType, Set<String> codes, String jsonGeom);

    /**
     * Generates a DistrictMap containing geometry that represents the area contained within the
     * supplied reference district codes of type refDistrictType. Useful for obtaining the polygon that
     * represents a collection of zip codes for example.
     * @param refDistrictType The reference district type.
     * @param refCodes        The reference district codes.
     * @return DistrictMap
     */
    DistrictMap getOverlapReferenceBoundary(DistrictType refDistrictType, Set<String> refCodes);

    /**
     * Retrieves a mapped collection of district code to DistrictMap that's grouped by DistrictType.
     * @return Map<DistrictType, Map<String, DistrictMap>>
     */
    Map<String, DistrictMap> getCodeToDistrictMapMap(DistrictType type);

    /**
     * Retrieves a mapped collection of DistrictMaps.
     * @return Map<DistrictType, List<DistrictMap>>
     */
    List<DistrictMap> getDistrictMaps(DistrictType type);

    /**
     * Fetches all the district maps from the database and stores them in a collection as well as
     * a lookup cache for fast retrieval.
     */
    boolean cacheDistrictMaps();

    Map<MunicipalityType, Map<String, Integer>> getTypeAndNameToIdMap();

    /**
     * Obtain a list of districts that are closest to the given point. This list does not include the
     * district that the point actually resides within.
     * @param districtType
     * @param point
     * @return
     */
    LinkedHashMap<String, DistrictMap> getNearbyDistricts(DistrictType districtType, Point point, boolean getMaps, int proximity, int count);


}
