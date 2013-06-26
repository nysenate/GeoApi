package gov.nysenate.sage.model.result;

public enum ResultStatus
{
    SUCCESS(0, "Success."),

    SERVICE_NOT_SUPPORTED(1, "The requested service is unsupported."),
    FEATURE_NOT_SUPPORTED(2, "The requested feature is unsupported."),
    PROVIDER_NOT_SUPPORTED(3, "The requested provider is unsupported."),
    ADDRESS_PROVIDER_NOT_SUPPORTED(4, "The requested address provider is unsupported."),
    GEOCODE_PROVIDER_NOT_SUPPORTED(5, "The requested geocoding provider is unsupported."),
    DISTRICT_PROVIDER_NOT_SUPPORTED(6, "The requested district assignment provider is unsupported."),

    API_KEY_INVALID(10, "The supplied API key could not be authenticated."),
    API_KEY_MISSING(11, "An API key is required."),

    API_REQUEST_INVALID(20, "The request is not in a valid format. Check the documentation for proper usage."),
    API_INPUT_FORMAT_UNSUPPORTED(21, "The requested input format is currently not supported."),
    API_OUTPUT_FORMAT_UNSUPPORTED(22, "The requested output format is currently not supported."),
    JSONP_CALLBACK_NOT_SPECIFIED(23, "A callback signature must be specified as a parameter e.g &callback=method"),

    RESPONSE_MISSING_ERROR(90, "No response from service provider."),
    RESPONSE_PARSE_ERROR(91, "Error parsing response from service provider."),

    MISSING_INPUT_PARAMS(100, "One or more parameters are missing."),
    MISSING_ADDRESS(110, "An address is required."),
    MISSING_GEOCODE(120, "A valid geocoded coordinate pair is required."),
    MISSING_ZIPCODE(130, "A zipcode is required."),
    MISSING_STATE(140, "A state is required."),
    MISSING_POINT(150, "A coordinate pair is required."),
    MISSING_GEOCODED_ADDRESS(160, "A valid geocoded address is required. The given address was likely unable to be matched using the geocoder."),

    INVALID_INPUT_PARAMS(200, "One or more parameters are invalid."),
    INVALID_ADDRESS(210, "The supplied address is invalid."),
    INVALID_GEOCODE(220, "The supplied geocoded coordinate pair is invalid."),
    INVALID_ZIPCODE(230, "The supplied zipcode is invalid."),
    INVALID_STATE(240, "The supplied state is invalid or is not supported."),
    INVALID_BATCH_ADDRESSES(250, "The supplied batch address list could not be parsed."),
    INVALID_BATCH_POINTS(260, "The supplied batch point list could not be parsed"),
    NON_NY_STATE(270, "Only New York State addresses are supported"),

    INSUFFICIENT_INPUT_PARAMS(300, "One or more parameters are insufficient."),
    INSUFFICIENT_ADDRESS(310, "The supplied address is missing one or more parameters."),
    INSUFFICIENT_GEOCODE(320, "The supplied geocoded is missing one or more parameters."),

    NO_DISTRICT_RESULT(400, "District assignment returned no results."),
    MULTIPLE_DISTRICT_RESULT(401, "Multiple matches were found for certain districts."),
    PARTIAL_DISTRICT_RESULT(402, "District assignment only yielded some of the districts requested."),
    NO_GEOCODE_RESULT(410, "Geocode service returned no results."),
    NO_REVERSE_GEOCODE_RESULT(411, "Reverse Geocode service returned no results."),
    NO_ADDRESS_VALIDATE_RESULT(420, "The address could not be validated."),
    NO_STREET_LOOKUP_RESULT(430, "Street lookup returned no results for the given zip5"),

    NO_MAP_RESULT(450, "Map request returned no results"),
    UNSUPPORTED_DISTRICT_MAP(460, "Maps for the requested district type are not available"),
    MISSING_DISTRICT_CODE(470, "A district code is required"),

    /** Unexpected errors */
    INTERNAL_ERROR(500, "Internal Server Error."),
    DATABASE_ERROR(501, "Database Error."),
    RESPONSE_ERROR(502, "Application failed to provide a response."),
    RESPONSE_SERIALIZATION_ERROR(503, "Failed to serialize response.");


    private int code;
    private String desc;
    ResultStatus(int code, String desc){
        this.code = code;
        this.desc = desc;
    }

    public int getCode()
    {
        return this.code;
    }

    public String getDesc()
    {
        return this.desc;
    }

}
