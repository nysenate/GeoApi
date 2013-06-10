package gov.nysenate.sage.model.api;

/**
 * Represents the uri information that is provided to request a service from the API. This object is
 * typically created by the API filter and consumed by the controller classes.
 */
public class ApiRequest
{
    /** Uri attributes */
    protected int version;
    protected String service;
    protected String request;
    protected boolean isBatch;

    /** Query string attributes */
    protected String provider;

    public ApiRequest(int version, String service, String request, boolean batch) {
        this.version = version;
        this.service = service;
        this.isBatch = batch;
        if (request != null) { this.request = request.toLowerCase().trim(); }
    }

    public int getVersion() {
        return version;
    }

    public String getService() {
        return service;
    }

    public String getRequest() {
        return request;
    }

    public boolean isBatch() {
        return isBatch;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
