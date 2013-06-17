package gov.nysenate.sage.model.stats;

import gov.nysenate.sage.model.api.ApiUser;

/**
 * Model class for representing usage counts for a specific ApiUser.
 */
public class ApiUserStats
{
    private ApiUser apiUser;
    private int dailyApiRequests;
    private int monthlyApiRequests;
    private int lifetimeApiRequests;

    public ApiUser getApiUser() {
        return apiUser;
    }

    public void setApiUser(ApiUser apiUser) {
        this.apiUser = apiUser;
    }
}
