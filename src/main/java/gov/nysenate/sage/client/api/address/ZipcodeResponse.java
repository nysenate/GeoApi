package gov.nysenate.sage.client.api.address;

import gov.nysenate.sage.client.api.BaseResponse;
import gov.nysenate.sage.model.result.AddressResult;

import static gov.nysenate.sage.model.result.ResultStatus.SUCCESS;

public class ZipcodeResponse extends BaseResponse
{
    protected String zip5;

    public ZipcodeResponse(AddressResult addressResult)
    {
        super(addressResult);
        if (addressResult != null && addressResult.getStatusCode().equals(SUCCESS)) {
            this.zip5 = addressResult.getAddress().getZip5();
        }
    }

    public String getZip5() {
        return zip5;
    }
}
