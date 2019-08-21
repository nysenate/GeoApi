package gov.nysenate.sage.service.map;

import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.provider.district.MapService;

import java.util.Map;

public interface SageMapServiceProvider {

    /**
     * Assigns district maps to a DistrictInfo result.
     * @param districtInfo DistrictInfo to set
     * @param override If false, previously set DistrictMaps will not be replaced
     * @return
     */
    public DistrictInfo assignMapsToDistrictInfo(DistrictInfo districtInfo, DistrictMatchLevel matchLevel, boolean override);

    /**
     * Returns the default MapService configured in the app properties
     * @return
     */
    public MapService getDefaultProvider();

    /**
     * Return a map containing a mapservice and its keyword
     * @return
     */
    public Map<String, MapService> getProviders();
}
