package gov.nysenate.sage.service.map;

import gov.nysenate.sage.dao.provider.DistrictShapefileDao;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.MapResult;
import gov.nysenate.sage.service.base.ServiceProviders;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

public class MapServiceProvider extends ServiceProviders<MapService>
{
    protected Logger logger = Logger.getLogger(MapServiceProvider.class);

    /**
     * Assigns district maps to a DistrictInfo result.
     * @param districtInfo DistrictInfo to set
     * @param override If false, previously set DistrictMaps will not be replaced
     * @return
     */
    public DistrictInfo assignMapsToDistrictInfo(DistrictInfo districtInfo, boolean override)
    {
        MapService mapService = this.newInstance();
        if (districtInfo != null && mapService != null) {
            for (DistrictType districtType : districtInfo.getAssignedDistricts()) {
                if (districtInfo.getDistMap(districtType) == null || override) {
                    MapResult mapResult = mapService.getDistrictMap(districtType, districtInfo.getDistCode(districtType));
                    if (mapResult.isSuccess()) {
                        districtInfo.setDistMap(districtType, mapResult.getDistrictMap());
                    }
                    else {
                        districtInfo.setDistMap(districtType, null);
                    }
                }
                /** Replace neighbor maps if they exist */
                List<DistrictMap> neighborMaps = districtInfo.getNeighborMaps(districtType);
                if (!neighborMaps.isEmpty()) {
                    List<DistrictMap> replaceMaps = new LinkedList<>();
                    for (DistrictMap map : neighborMaps) {
                        MapResult mapResult = mapService.getDistrictMap(districtType, map.getDistrictCode());
                        replaceMaps.add((mapResult.isSuccess() ? mapResult.getDistrictMap() : map));
                    }
                    districtInfo.addNeighborMaps(districtType, replaceMaps);
                }
            }
        }
        return districtInfo;
    }
}
