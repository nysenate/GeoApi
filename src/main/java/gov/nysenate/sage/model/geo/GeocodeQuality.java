package gov.nysenate.sage.model.geo;

/**
 * GeocodeQuality is a simplified measure of the accuracy of the
 * geocoding result provided by a service.
 */
public enum GeocodeQuality
{
    NOMATCH(0), UNKNOWN(1), STATE(10), COUNTY(30), CITY(40), ZIP(64), STREET(72), ZIP_EXT(75), HOUSE(87), POINT(99);

    private int quality;

    GeocodeQuality(int quality)
    {
        this.quality = quality;
    }

    public int getQuality()
    {
        return this.quality;
    }
}