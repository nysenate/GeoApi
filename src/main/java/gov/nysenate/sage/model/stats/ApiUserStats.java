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

    public int getDailyApiRequests() {
        return dailyApiRequests;
    }

    public void setDailyApiRequests(int dailyApiRequests) {
        this.dailyApiRequests = dailyApiRequests;
    }

    public int getMonthlyApiRequests() {
        return monthlyApiRequests;
    }

    public void setMonthlyApiRequests(int monthlyApiRequests) {
        this.monthlyApiRequests = monthlyApiRequests;
    }

    public int getLifetimeApiRequests() {
        return lifetimeApiRequests;
    }

    public void setLifetimeApiRequests(int lifetimeApiRequests) {
        this.lifetimeApiRequests = lifetimeApiRequests;
    }
}
