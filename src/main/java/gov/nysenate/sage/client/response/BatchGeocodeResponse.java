package gov.nysenate.sage.client.response;

import gov.nysenate.sage.model.result.GeocodeResult;

import java.util.ArrayList;
import java.util.List;

public class BatchGeocodeResponse extends BatchResponse<GeocodeResponse>
{
    public BatchGeocodeResponse(List<GeocodeResult> results) {
        super();
        List<GeocodeResponse> responses = new ArrayList<>();
        for (GeocodeResult geocodeResult : results) {
            responses.add(new GeocodeResponse(geocodeResult));
        }
        this.results = responses;
    }
}
