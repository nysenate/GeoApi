package gov.nysenate.sage.model.stats;

/**
 * Represents the degrees in which API usage is logged to the database.
 */
public enum ApiLogLevel
{
    NONE,
    LOG_BASIC,
    LOG_REQUESTS,
    LOG_REQUESTS_AND_RESPONSES,
    LOG_ALL;
}
