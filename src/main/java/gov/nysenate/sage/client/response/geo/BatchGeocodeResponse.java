package gov.nysenate.sage.client.response.geo;

import gov.nysenate.sage.client.response.base.BatchResponse;
import gov.nysenate.sage.model.result.GeocodeResult;

import java.util.List;

public class BatchGeocodeResponse extends BatchResponse<GeocodeResponse> {
    public BatchGeocodeResponse(List<GeocodeResult> results) {
        super(results, GeocodeResponse::new);
    }
}
