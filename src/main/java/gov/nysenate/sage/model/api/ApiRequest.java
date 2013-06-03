package gov.nysenate.sage.model.api;

import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Represents the uri information that is provided to request a service from the API. This object is
 * typically created by the API filter and consumed by the controller classes.
 */
public class ApiRequest
{
    protected int id;

    /** Authentication */
    protected ApiUser apiUser;
    protected InetAddress ipAddress;

    /** Uri attributes */
    protected int version;
    protected String service;
    protected String request;
    protected boolean isBatch;

    /** Query string attributes */
    protected String provider;

    /** Timing information */
    protected Timestamp apiRequestTime;

    public ApiRequest(int version, String service, String request, boolean batch, InetAddress ipAddress) {
        this.version = version;
        this.service = service;
        this.isBatch = batch;
        if (request != null) { this.request = request.toLowerCase().trim(); }
        this.apiRequestTime = new Timestamp(new Date().getTime());
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

    public void setIpAddress(InetAddress ipAddress) {
        this.ipAddress = ipAddress;
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

    public void setApiRequestTime(Timestamp apiRequestTime)
    {
        this.apiRequestTime = apiRequestTime;
    }

    public Timestamp getApiRequestTime()
    {
        return this.apiRequestTime;
    }
}