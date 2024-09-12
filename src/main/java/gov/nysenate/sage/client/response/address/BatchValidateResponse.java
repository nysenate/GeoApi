package gov.nysenate.sage.client.response.address;

import gov.nysenate.sage.client.response.base.BatchResponse;
import gov.nysenate.sage.model.result.AddressResult;

import java.util.List;

public class BatchValidateResponse extends BatchResponse<ValidateResponse> {
    public BatchValidateResponse(List<AddressResult> addressResults) {
        super(addressResults, ValidateResponse::new);
    }
}
