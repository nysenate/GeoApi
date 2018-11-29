package gov.nysenate.sage.model.api;

public class ApiUserAuthEvictEvent {

    protected String apiKey;

    public ApiUserAuthEvictEvent(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }
}
