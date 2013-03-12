package gov.nysenate.sage.client.api.address;

import gov.nysenate.sage.client.api.BaseResponse;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.ResultStatus;

/** Represents the client side API response for a citystate lookup request */
public class CityStateResponse extends BaseResponse
{
    protected String city = "";
    protected String state = "";
    protected String zip5 = "";

    public CityStateResponse(AddressResult addressResult)
    {
        super();
        if (addressResult != null) {
            this.status = addressResult.getStatusCode();
            this.messages = addressResult.getMessages();
            this.source = addressResult.getSource();

            if (addressResult.getStatusCode().equals(ResultStatus.SUCCESS)) {
                this.city = addressResult.getAddress().getCity();
                this.state = addressResult.getAddress().getState();
                this.zip5 = addressResult.getAddress().getZip5();
            }
        }
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZip5() {
        return zip5;
    }
}