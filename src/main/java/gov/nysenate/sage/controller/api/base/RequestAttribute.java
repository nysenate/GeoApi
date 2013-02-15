package gov.nysenate.sage.controller.api.base;

/**
 *
 */
public enum RequestAttribute
{
    REQUEST_TYPE("request_type"), FORMAT("format"), PARAM_SOURCE("param_source"), PARAM_TYPE("param_type");

    String name;
    RequestAttribute(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
