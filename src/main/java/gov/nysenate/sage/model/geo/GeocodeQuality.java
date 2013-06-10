package gov.nysenate.sage.model.geo;

/**
 * GeocodeQuality is a simplified accuracy measure of the geocoding result. The values have
 * been assigned based on similar quality codes among different geocoders.
 */
public enum GeocodeQuality
{
    NOMATCH(0), STATE(10), COUNTY(30), CITY(40), UNKNOWN(64), ZIP(64), STREET(72), ZIP_EXT(75), HOUSE(87), POINT(99);

    private int value;

    GeocodeQuality(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return this.value;
    }
}