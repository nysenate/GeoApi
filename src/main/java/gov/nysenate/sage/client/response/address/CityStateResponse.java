package gov.nysenate.sage.client.response.address;

import gov.nysenate.sage.client.response.base.SourcedResponse;
import gov.nysenate.sage.model.result.CityStateResult;

import javax.annotation.Nonnull;

/** Represents the client side API response for a citystate lookup request */
public class CityStateResponse extends SourcedResponse {
    private final CityStateResult result;

    public CityStateResponse(@Nonnull CityStateResult cityStateResult) {
        super(cityStateResult);
        this.result = cityStateResult;
    }

    public String getCity() {
        return result.getPostalCity();
    }

    public String getState() {
        return result.getState();
    }

    public Integer getZip5() {
        return result.getZip5();
    }
}