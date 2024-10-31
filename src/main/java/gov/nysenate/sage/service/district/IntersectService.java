package gov.nysenate.sage.service.district;

import gov.nysenate.sage.model.api.IntersectRequest;
import gov.nysenate.sage.model.result.IntersectResult;
import gov.nysenate.sage.provider.district.DistrictShapefile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IntersectService {
    private final DistrictShapefile shapefile;

    @Autowired
    public IntersectService(DistrictShapefile shapefile) {
        this.shapefile = shapefile;
    }

    /**
     * Handle intersect requests and executes functions based on settings in the supplied DistrictRequest.
     *
     * @param districtRequest Contains the various parameters for the District Assign/Bluebird API
     * @return DistrictResult
     */
    public IntersectResult handleIntersectRequest(IntersectRequest districtRequest) {
        // TODO: log intersect request
        // sqlDistrictRequestLogger.logDistrictRequest(districtRequest);
        IntersectResult districtResult = shapefile.getIntersectionResult(districtRequest.sourceType(),
                districtRequest.sourceId(), districtRequest.intersectWith());
        districtResult.setResultTime();
        return districtResult;
    }
}
