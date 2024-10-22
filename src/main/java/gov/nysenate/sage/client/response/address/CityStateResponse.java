package gov.nysenate.sage.client.response.address;

import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.ResultStatus;

import javax.annotation.Nonnull;

/** Represents the client side API response for a citystate lookup request */
public class CityStateResponse extends BaseResponse {
    private String city = "";
    private String state = "";
    private Integer zip5 = null;

    public CityStateResponse(@Nonnull AddressResult addressResult) {
        super(addressResult);
        if (addressResult.getStatusCode().equals(ResultStatus.SUCCESS) && addressResult.getAddress() != null) {
            this.city = addressResult.getAddress().getPostalCity();
            this.state = addressResult.getAddress().getState();
            this.zip5 = addressResult.getAddress().getZip5();
        }
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public Integer getZip5() {
        return zip5;
    }
}