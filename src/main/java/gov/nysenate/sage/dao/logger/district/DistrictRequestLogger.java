package gov.nysenate.sage.dao.logger.district;

import gov.nysenate.sage.model.api.DistrictRequest;

public interface DistrictRequestLogger {
    /**
     * Log a DistrictRequest to the database
     * @param dr DistrictRequest
     * @return id of district request. This id is set to the supplied districtRequest as well.
     */
    int logDistrictRequest(DistrictRequest dr);
}
