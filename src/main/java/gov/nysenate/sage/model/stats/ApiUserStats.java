package gov.nysenate.sage.model.stats;

import gov.nysenate.sage.model.api.ApiUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model class for representing usage counts for a specific ApiUser.
 */
public class ApiUserStats
{
    private ApiUser apiUser;
    private int apiRequests;
    private int geoRequests;
    private int distRequests;
    /**             (Service -> Method) -> RequestCount  */
    private Map<String, Map<String, Integer>> requestsByMethod = new HashMap<>();

    public ApiUser getApiUser() {
        return apiUser;
    }

    public void setApiUser(ApiUser apiUser) {
        this.apiUser = apiUser;
    }

    public int getApiRequests() {
        return apiRequests;
    }

    public void setApiRequests(int apiRequests) {
        this.apiRequests = apiRequests;
    }

    public int getGeoRequests() {
        return geoRequests;
    }

    public void setGeoRequests(int geoRequests) {
        this.geoRequests = geoRequests;
    }

    public int getDistRequests() {
        return distRequests;
    }

    public void setDistRequests(int distRequests) {
        this.distRequests = distRequests;
    }

    public Map<String, Map<String, Integer>> getRequestsByMethod() {
        return requestsByMethod;
    }

    public void setRequestsByMethod(Map<String, Map<String, Integer>> requestsByMethod) {
        this.requestsByMethod = requestsByMethod;
    }

    public void addMethodRequestCount(String service, String method, Integer requests)
    {
        if (this.requestsByMethod.get(service) == null) {
            this.requestsByMethod.put(service, new HashMap<String, Integer>());
        }
        this.requestsByMethod.get(service).put(method, requests);
    }
}
