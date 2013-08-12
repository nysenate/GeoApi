package gov.nysenate.sage.client.response.address;

import gov.nysenate.sage.client.response.base.BatchResponse;
import gov.nysenate.sage.model.result.AddressResult;

import java.util.ArrayList;
import java.util.List;

public class BatchValidateResponse extends BatchResponse<ValidateResponse>
{
    public BatchValidateResponse(List<AddressResult> addressResults) {
        super();
        List<ValidateResponse> responses = new ArrayList<>();
        for (AddressResult addressResult : addressResults) {
            responses.add(new ValidateResponse(addressResult));
        }
        this.results = responses;
    }
}
