package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

/**
 * Allows for thorough sorting of lines for QA purposes and error reporting.
 */
public enum StreetfileLineType {
    PROPER, SKIP, WRONG_LENGTH, BAD_BUILDING_NUMBER,
    MISSING_ADDRESS_DATA, TWO_ADDRESS_TYPES, UNPARSED_NON_STANDARD_ADDRESS,
    UNKNOWN_ERROR
}
