package gov.nysenate.sage.model.geo;

/**
 * GeocodeQuality is a simplified measure of the accuracy of the
 * geocoding result provided by a service.
 */
public enum GeocodeQuality
{
    NOMATCH(0), UNKNOWN(1), ZIP(2), ZIP_EXT(3), STREET(4), HOUSE(5);

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