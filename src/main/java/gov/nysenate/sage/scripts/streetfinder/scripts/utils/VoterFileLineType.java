package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

/**
 * Allows for thorough sorting of lines for QA purposes and error reporting.
 */
public enum VoterFileLineType {
    VALID, NON_STANDARD_ADDRESS, TWO_ADDRESS_FORMATS, MISSING_ZIP_5, HAS_TABS, WRONG_FIELD_LENGTH, NO_ADDRESS
}
