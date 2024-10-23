package gov.nysenate.sage.dao.provider.geoserver;

import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Point;

import java.util.List;

public interface GeoserverDao {
    /**
     * Submits a url request to GeoServer, parses response, and returns a DistrictInfo object
     *
     * @param point            The Point to find districts at
     * @param districtTypes    The types of districts to get features for
     * @return DistrictInfo if successful, null otherwise
     */
    DistrictInfo getDistrictInfo(Point point, List<DistrictType> districtTypes);
}
