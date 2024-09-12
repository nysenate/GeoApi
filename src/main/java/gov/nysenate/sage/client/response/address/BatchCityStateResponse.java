package gov.nysenate.sage.client.response.address;

import gov.nysenate.sage.client.response.base.BatchResponse;
import gov.nysenate.sage.model.result.AddressResult;

import java.util.List;

public class BatchCityStateResponse extends BatchResponse<CityStateResponse> {
    public BatchCityStateResponse(List<AddressResult> addressResults) {
        super(addressResults, CityStateResponse::new);
    }
}
