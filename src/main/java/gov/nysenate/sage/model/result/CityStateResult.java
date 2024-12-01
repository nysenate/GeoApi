package gov.nysenate.sage.model.result;

import gov.nysenate.sage.provider.address.AddressSource;

public class CityStateResult extends BaseResult<AddressSource> {
    private String postalCity = null;
    private String state = null;
    private Integer zip5 = null;

    public CityStateResult(AddressSource source, ResultStatus status) {
        super(source);
        setStatusCode(status);
    }

    public CityStateResult(AddressSource source, String postalCity, String state, Integer zip5) {
        super(source);
        this.postalCity = postalCity;
        this.state = state;
        this.zip5 = zip5;
    }

    public String getPostalCity() {
        return postalCity;
    }

    public String getState() {
        return state;
    }

    public Integer getZip5() {
        return zip5;
    }
}
