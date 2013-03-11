package gov.nysenate.sage.model.api;

/**
 * Represents the uri information that is provided to request a service from the API. This object is
 * typically created by the API filter and consumed by the controller classes.
 */
public class ApiRequest
{
    protected int version;
    protected String service;
    protected String request;
    protected boolean isBatch;

    public ApiRequest(int version, String service, String request, boolean batch) {
        this.version = version;
        this.service = service;
        this.request = request;
        this.isBatch = batch;
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
}
