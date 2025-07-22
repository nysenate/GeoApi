package gov.nysenate.sage.client.response.address;

import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.model.result.CityStateResult;

import javax.annotation.Nonnull;

/** Represents the client side API response for a citystate lookup request */
public class CityStateResponse extends BaseResponse {
    private String city = "";
    private String state = "";
    private Integer zip5 = null;

    public CityStateResponse(@Nonnull CityStateResult cityStateResult) {
        super(cityStateResult);
        if (cityStateResult.isSuccess()) {
            this.city = cityStateResult.getPostalCity();
            this.state = cityStateResult.getState();
            this.zip5 = cityStateResult.getZip5();
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