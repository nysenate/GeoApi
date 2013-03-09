package gov.nysenate.sage.controller.api;

/**
 *
 */
public enum RequestAttribute
{
    API_TYPE("apiType"), REQUEST_TYPE("requestType"), FORMAT("format"), PARAM_SOURCE("paramSource");

    private String name;
    RequestAttribute(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
