package gov.nysenate.sage.model.api;

import gov.nysenate.sage.model.job.JobProcess;
import gov.nysenate.sage.provider.geocode.Geocoder;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public abstract class GeocodeRequest {
    private final List<Geocoder> geocoders;
    private final Timestamp requestTime = new Timestamp(new Date().getTime());
    private final boolean doNotCache;

    private boolean isReverse;
    private boolean isUspsValidate;
    private ApiRequest apiRequest;
    private JobProcess jobProcess;

    public GeocodeRequest(ApiRequest apiRequest, List<Geocoder> geocoders,
                          boolean isReverse, boolean doNotCache, boolean isUspsValidate) {
        this.isReverse = isReverse;
        this.apiRequest = apiRequest;
        this.doNotCache = doNotCache;
        this.geocoders = geocoders;
        this.isUspsValidate = isUspsValidate;
    }

    public ApiRequest getApiRequest() {
        return apiRequest;
    }

    public void setApiRequest(ApiRequest apiRequest) {
        this.apiRequest = apiRequest;
    }

    public List<Geocoder> getGeocoders() {
        return geocoders;
    }

    public JobProcess getJobProcess() {
        return jobProcess;
    }

    public void setJobProcess(JobProcess jobProcess) {
        this.jobProcess = jobProcess;
    }

    public boolean isReverse() {
        return isReverse;
    }

    public void setReverse(boolean reverse) {
        isReverse = reverse;
    }

    public Timestamp getRequestTime() {
        return requestTime;
    }

    public boolean isDoNotCache() {
        return doNotCache;
    }

    public boolean isUspsValidate() {
        return isUspsValidate;
    }

    public void setUspsValidate(boolean uspsValidate) {
        isUspsValidate = uspsValidate;
    }
}
