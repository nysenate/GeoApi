package gov.nysenate.sage.client.response.address;

import gov.nysenate.sage.client.response.base.BatchResponse;
import gov.nysenate.sage.model.result.AddressResult;

import java.util.ArrayList;
import java.util.List;

public class BatchCityStateResponse extends BatchResponse<CityStateResponse>
{
    public BatchCityStateResponse(List<AddressResult> addressResults) {
        super();
        List<CityStateResponse> responses = new ArrayList<>();
        for (AddressResult addressResult : addressResults) {
            responses.add(new CityStateResponse(addressResult));
        }
        this.results = responses;
    }
}
