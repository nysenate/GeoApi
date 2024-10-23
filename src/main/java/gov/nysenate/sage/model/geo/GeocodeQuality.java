package gov.nysenate.sage.model.geo;

/**
 * GeocodeQuality is a simplified accuracy measure of the geocoding result. The values have
 * been assigned based on similar quality codes among different geocoders.
 */
public enum GeocodeQuality {
    NOMATCH, STATE, COUNTY, CITY, UNKNOWN,
    ZIP, STREET, ZIP_EXT, HOUSE, POINT;

    public static GeocodeQuality fromString(String quality) {
        try {
            return valueOf(quality.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}