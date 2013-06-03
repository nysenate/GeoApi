package gov.nysenate.sage.model.api;

import gov.nysenate.sage.model.address.Address;

import java.sql.Timestamp;
import java.util.Date;

public class GeocodeRequest
{
    private ApiRequest apiRequest;
    private Address address;
    private String provider;
    private boolean useFallback;
    private boolean useCache;
    private Timestamp requestTime;

    public GeocodeRequest(ApiRequest apiRequest, Address address, String provider, boolean useFallback, boolean useCache)
    {
        this.apiRequest = apiRequest;
        this.address = address;
        this.provider = provider;
        this.useFallback = useFallback;
        this.useCache = useCache;
        this.requestTime = new Timestamp(new Date().getTime());
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

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Timestamp getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Timestamp requestTime) {
        this.requestTime = requestTime;
    }
}
