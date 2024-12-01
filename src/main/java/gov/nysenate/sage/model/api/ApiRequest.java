package gov.nysenate.sage.model.api;

import gov.nysenate.sage.util.FormatUtil;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Represents the uri information that is provided to request a service from the API. This object is
 * typically created by the API filter and consumed by the controller classes.
 */
public class ApiRequest {
    private int id;

    /** Authentication */
    private ApiUser apiUser;
    private final InetAddress ipAddress;

    /** Uri attributes */
    private String service;
    private String request;
    private final boolean isBatch;

    /** Query string attributes */
    private String provider;

    /** Timing information */
    private final Timestamp apiRequestTime = new Timestamp(new Date().getTime());

    public ApiRequest(String service, String request, boolean batch, InetAddress ipAddress) {
        if (service != null) {
            this.service = FormatUtil.cleanString(service);
        }
        this.isBatch = batch;
        if (request != null) {
            this.request = FormatUtil.cleanString(request.toLowerCase().trim());
        }
        this.ipAddress = ipAddress;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ApiUser getApiUser() {
        return apiUser;
    }

    public void setApiUser(ApiUser apiUser) {
        this.apiUser = apiUser;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
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

    // TODO: use
    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        if (provider != null) {
            this.provider = FormatUtil.cleanString( provider );
        }
    }

    public Timestamp getApiRequestTime()
    {
        return this.apiRequestTime;
    }
}