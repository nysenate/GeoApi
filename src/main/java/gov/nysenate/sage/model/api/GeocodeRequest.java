package gov.nysenate.sage.model.api;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.job.JobProcess;

import java.sql.Timestamp;
import java.util.Date;

public class GeocodeRequest implements Cloneable
{
    private int id;
    private int addressId;

    /** Source identifiers */
    private ApiRequest apiRequest;
    private JobProcess jobProcess;

    /** Inputs */
    private Address address;
    private Point point;
    private boolean isReverse;

    private String provider;
    private boolean useFallback;
    private boolean useCache;
    private Timestamp requestTime;
    private boolean bypassCache;
    private boolean doNotCache;

    public GeocodeRequest() {}

    public GeocodeRequest(ApiRequest apiRequest, Address address, String provider, boolean useFallback, boolean useCache)
    {
        this.apiRequest = apiRequest;
        this.address = address;
        this.provider = provider;
        this.useFallback = useFallback;
        this.useCache = useCache;
        this.isReverse = false;
        this.requestTime = new Timestamp(new Date().getTime());
        this.bypassCache = false;
    }

    public GeocodeRequest(ApiRequest apiRequest, Address address, String provider, boolean useFallback, boolean useCache, boolean bypassCache, boolean doNotCache)
    {
        this.apiRequest = apiRequest;
        this.address = address;
        this.provider = provider;
        this.useFallback = useFallback;
        this.useCache = useCache;
        this.isReverse = false;
        this.requestTime = new Timestamp(new Date().getTime());
        this.bypassCache = bypassCache;
        this.doNotCache = doNotCache;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public ApiRequest getApiRequest() {
        return apiRequest;
    }

    public void setApiRequest(ApiRequest apiRequest) {
        this.apiRequest = apiRequest;
    }

    public JobProcess getJobProcess() {
        return jobProcess;
    }

    public void setJobProcess(JobProcess jobProcess) {
        this.jobProcess = jobProcess;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public boolean isReverse() {
        return isReverse;
    }

    public void setReverse(boolean reverse) {
        isReverse = reverse;
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

    public boolean isBypassCache() {
        return bypassCache;
    }

    public void setBypassCache(boolean bypassCache) {
        this.bypassCache = bypassCache;
    }

    public boolean isDoNotCache() {
        return doNotCache;
    }

    public void setDoNotCache(boolean doNotCache) {
        this.doNotCache = doNotCache;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}