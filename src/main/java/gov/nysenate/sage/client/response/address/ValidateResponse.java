package gov.nysenate.sage.client.response.address;

import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.view.address.AddressView;
import gov.nysenate.sage.model.result.AddressResult;

import static gov.nysenate.sage.model.result.ResultStatus.SUCCESS;

public class ValidateResponse extends BaseResponse
{
    protected AddressView address;
    protected boolean validated = false;

    public ValidateResponse(AddressResult addressResult)
    {
        super(addressResult);
        if (addressResult != null && addressResult.getStatusCode() != null) {
            this.validated = addressResult.isValidated();
            this.address = new AddressView(addressResult.getAddress());
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
