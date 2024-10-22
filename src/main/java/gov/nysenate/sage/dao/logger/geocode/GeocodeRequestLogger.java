package gov.nysenate.sage.dao.logger.geocode;

import gov.nysenate.sage.model.api.SingleGeocodeRequest;

public interface GeocodeRequestLogger {

    /**
     * Log a GeocodeRequest to the database
     * @param geoRequest
     * @return id of geocode request. This id is set to the supplied GeocodeRequest as well.
     */
    int logGeocodeRequest(SingleGeocodeRequest geoRequest);
}
