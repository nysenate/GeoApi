package gov.nysenate.sage.model.stats;

import gov.nysenate.sage.model.api.ApiUser;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class for representing usage counts for a specific ApiUser.
 */
public class ApiUserStats {
    private final ApiUser apiUser;

    // (Service -> Method) -> RequestCount
    private final Map<String, Map<String, Integer>> requestsByMethod = new HashMap<>();

    public ApiUserStats(ApiUser apiUser) {
        this.apiUser = apiUser;
    }

    public ApiUser getApiUser() {
        return apiUser;
    }

    public int getApiRequests() {
        int count = 0;
        for (var entry : requestsByMethod.entrySet()) {
            count += entry.getValue().size();
        }
        return count;
    }

    public int getGeoRequests() {
        return requestsByMethod.getOrDefault("geo", Map.of()).getOrDefault("geocode", 0);
    }

    public int getDistRequests() {
        return requestsByMethod.getOrDefault("district", Map.of()).getOrDefault("assign", 0);
    }

    public Map<String, Map<String, Integer>> getRequestsByMethod() {
        return requestsByMethod;
    }

    public void addMethodRequestCount(String service, String method) {
        this.requestsByMethod.computeIfAbsent(service, k -> new HashMap<>());
        this.requestsByMethod.get(service).merge(method, 1, Integer::sum);
    }
}
