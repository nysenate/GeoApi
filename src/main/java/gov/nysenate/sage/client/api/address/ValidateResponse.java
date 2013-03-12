package gov.nysenate.sage.client.api.address;

import gov.nysenate.sage.client.api.BaseResponse;
import gov.nysenate.sage.client.model.SageAddress;
import gov.nysenate.sage.model.result.AddressResult;
import static gov.nysenate.sage.model.result.ResultStatus.*;

public class ValidateResponse extends BaseResponse
{
    protected SageAddress address;
    protected boolean validated = false;

    public ValidateResponse(AddressResult addressResult)
    {
        super(addressResult);
        if (addressResult != null && addressResult.getStatusCode().equals(SUCCESS)) {
            this.address = new SageAddress(addressResult.getAddress());
            this.validated = addressResult.isValidated();
        }
    }

    public SageAddress getAddress()
    {
        return address;
    }

    public boolean isValidated()
    {
        return validated;
    }
}
