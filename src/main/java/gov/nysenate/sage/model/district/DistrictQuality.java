package gov.nysenate.sage.model.district;

/**
 * DistrictQuality is a simplified accuracy measure of the district assignment result. The
 * numerical values are chosen somewhat arbitrarily to instill a ranking.
 */
public enum DistrictQuality
{
    NOMATCH(0), ZIP5(59), STREET(79), HOUSE(95), POINT(99);

    private int value;

    DistrictQuality(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return this.value;
    }
}
