package gov.nysenate.sage.client.response.address;

import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.model.result.AddressResult;

import static gov.nysenate.sage.model.result.ResultStatus.SUCCESS;

public class ZipcodeResponse extends BaseResponse
{
    protected String zip5;
    protected String zip4;

    public ZipcodeResponse(AddressResult addressResult)
    {
        super(addressResult);
        if (addressResult != null && addressResult.getStatusCode().equals(SUCCESS)) {
            this.zip5 = addressResult.getAddress().getZip5();
            this.zip4 = addressResult.getAddress().getZip4();
        }
    }

    public String getZip5() {
        return zip5;
    }

    public String getZip4() {
        return zip4;
    }
}
