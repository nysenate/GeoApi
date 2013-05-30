package gov.nysenate.sage.model.api;

import gov.nysenate.sage.model.address.Address;

public class GeoRequest
{
    private ApiRequest apiRequest;
    private Address address;
    private boolean useFallback;
    private boolean useCache;

    public GeoRequest(ApiRequest apiRequest) {
        this.apiRequest = apiRequest;
    }

    public ApiRequest getApiRequest() {
        return apiRequest;
    }

    public void setApiRequest(ApiRequest apiRequest) {
        this.apiRequest = apiRequest;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public boolean isUseFallback() {
        return useFallback;
    }

    public void setUseFallback(boolean useFallback) {
        this.useFallback = useFallback;
    }

    public boolean isUseCache() {
        return useCache;
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }
}
