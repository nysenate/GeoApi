package gov.nysenate.sage.model.result;

public enum ResultStatus {
    SUCCESS(0, "Success."),

    SERVICE_NOT_SUPPORTED(1, "The requested service is unsupported."),
    FEATURE_NOT_SUPPORTED(2, "The requested feature is unsupported."),
    PROVIDER_NOT_SUPPORTED(3, "The requested provider is unsupported."),
    ADDRESS_PROVIDER_NOT_SUPPORTED(4, "The requested address provider is unsupported."),
    GEOCODE_PROVIDER_NOT_SUPPORTED(5, "The requested geocoding provider is unsupported."),
    DISTRICT_PROVIDER_NOT_SUPPORTED(6, "The requested district assignment provider is unsupported."),
    GEOCODE_PROVIDER_TEMP_DISABLED(7, "The geocode provider is temporarily disabled."),
    GEOCODE_PROVIDER_DISABLED(8, "The geocode provider is disabled."),

    API_KEY_INVALID(10, "The supplied API key could not be authenticated."),
    API_KEY_MISSING(11, "An API key is required."),
    CONFIG_FILE_MISSING(12, "The configuration file could not be located."),

    API_REQUEST_INVALID(20, "The request is not in a valid format. Check the documentation for proper usage."),
    API_INPUT_FORMAT_UNSUPPORTED(21, "The requested input format is currently not supported."),
    API_OUTPUT_FORMAT_UNSUPPORTED(22, "The requested output format is currently not supported."),
    JSONP_CALLBACK_NOT_SPECIFIED(23, "A callback signature must be specified as a parameter e.g &callback=method"),

    RESPONSE_MISSING_ERROR(30, "No response from service provider."),
    RESPONSE_PARSE_ERROR(31, "Error parsing response from service provider."),

    MISSING_INPUT_PARAMS(40, "One or more parameters are missing."),
    MISSING_ADDRESS(41, "An address is required."),
    MISSING_GEOCODE(42, "A valid geocoded coordinate pair is required."),
    MISSING_ZIPCODE(43, "A zipcode is required."),
    MISSING_STATE(44, "A state is required."),
    MISSING_POINT(45, "A coordinate pair is required."),
    MISSING_GEOCODED_ADDRESS(46, "The address was unable to be matched using the geocoding service. Please ensure that the address is valid " +
                                  "or try using a different geocoder."),

    INVALID_INPUT_PARAMS(50, "One or more parameters are invalid."),
    INVALID_ADDRESS(51, "The supplied address is invalid."),
    INVALID_GEOCODE(52, "The geocoding process did not yield a successful response. Please ensure that you have entered a valid address or try selecting a different geocoding provider."),
    INVALID_ZIPCODE(53, "The supplied zipcode is invalid."),
    INVALID_STATE(54, "The supplied state is invalid or is not supported."),
    INVALID_BATCH_ADDRESSES(55, "The supplied batch address list could not be parsed."),
    INVALID_BATCH_POINTS(56, "The supplied batch point list could not be parsed"),
    NON_NY_STATE(57, "The address you have supplied is not a valid New York address. Only NY addresses are supported at this time."),

    INSUFFICIENT_INPUT_PARAMS(60, "One or more parameters are insufficient."),
    INSUFFICIENT_ADDRESS(61, "The supplied address does not contain enough information to continue processing. Try adding a city, state, or zip."),
    INSUFFICIENT_GEOCODE(62, "The supplied geocode does not contain enough information to continue processing."),

    NO_DISTRICT_RESULT(70, "District assignment returned no results."),
    NO_GEOCODE_RESULT(71, "Geocode service returned no results."),
    NO_REVERSE_GEOCODE_RESULT(72, "Reverse Geocode service returned no results."),
    NO_ADDRESS_VALIDATE_RESULT(73, "The address could not be validated."),
    NO_STREET_LOOKUP_RESULT(74, "Street lookup returned no results for the given zip5"),

    NO_MAP_RESULT(80, "Map request returned no results"),
    UNSUPPORTED_DISTRICT_MAP(81, "Maps for the requested district type are not available"),
    MISSING_DISTRICT_CODE(82, "A district code is required"),

    NO_STREETFILES_TO_PROCESS(90, "There were no streetfiles to process. No action was taken."),

    /** Unexpected errors */
    NOT_FOUND(404, "Not Found"),
    INTERNAL_ERROR(500, "Internal Server Error."),
    DATABASE_ERROR(501, "Database Error."),
    RESPONSE_ERROR(502, "Application failed to provide a response."),
    RESPONSE_SERIALIZATION_ERROR(503, "Failed to serialize response.");

    private final int code;
    private final String desc;

    ResultStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return this.code;
    }

    public String getDesc() {
        return this.desc;
    }
}
