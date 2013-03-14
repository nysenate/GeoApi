package gov.nysenate.sage.client.response;

import gov.nysenate.sage.client.response.BaseResponse;
import gov.nysenate.sage.client.view.AddressView;
import gov.nysenate.sage.model.result.AddressResult;
import static gov.nysenate.sage.model.result.ResultStatus.*;

public class ValidateResponse extends BaseResponse
{
    protected AddressView address;
    protected boolean validated = false;

    public ValidateResponse(AddressResult addressResult)
    {
        super(addressResult);
        if (addressResult != null && addressResult.getStatusCode().equals(SUCCESS)) {
            this.address = new AddressView(addressResult.getAddress());
            this.validated = addressResult.isValidated();
        }
    }

    public AddressView getAddress()
    {
        return address;
    }

    public boolean isValidated()
    {
        return validated;
    }
}
