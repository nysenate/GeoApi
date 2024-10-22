package gov.nysenate.sage.service.map;

import gov.nysenate.sage.model.district.*;
import gov.nysenate.sage.model.result.MapResult;
import gov.nysenate.sage.provider.district.DistrictShapefile;
import gov.nysenate.sage.provider.district.MapService;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class MapServiceProvider implements SageMapServiceProvider
{
    protected Logger logger = LoggerFactory.getLogger(MapServiceProvider.class);

    protected MapService defaultProvider;
    protected Map<String,MapService> providers = new HashMap<>();

    @Autowired
    public MapServiceProvider(DistrictShapefile districtShapefile) {
        this.defaultProvider = districtShapefile;
        providers.put("shapefile", this.defaultProvider);
    }


    /**
     * Assigns district maps to a DistrictInfo result.
     * @param districtInfo DistrictInfo to set
     * @param override If false, previously set DistrictMaps will not be replaced
     * @return
     */
    public DistrictInfo assignMapsToDistrictInfo(DistrictInfo districtInfo, DistrictMatchLevel matchLevel, boolean override)
    {
        MapService mapService = this.defaultProvider;
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
            /** Fill in senate overlap maps as well */
            if (!districtInfo.getDistrictOverlaps().isEmpty()) {
                logger.trace("Getting overlap maps too!");
                for (DistrictType districtType : DistrictType.getStandardTypes()){
                    if (districtInfo.getDistrictOverlap(districtType) != null) {
                        DistrictOverlap senateOverlap = districtInfo.getDistrictOverlap(districtType);
                        for (String code : senateOverlap.getTargetOverlap().keySet()) {
                            MapResult mapResult = mapService.getDistrictMap(districtType, code);
                            if (mapResult.isSuccess()) {
                                senateOverlap.setTargetDistrictMap(code, mapResult.getDistrictMap());
                            }
                        }
                    }
                }
            }
        }
        return districtInfo;
    }

    public Map<String, MapService> getProviders() {
        return providers;
    }
}
