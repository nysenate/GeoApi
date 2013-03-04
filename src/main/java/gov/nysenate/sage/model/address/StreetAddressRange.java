package gov.nysenate.sage.model.address;

/**
 * Represents a range of street addresses
 */
public class StreetAddressRange
{
    StreetAddress loStreetAddress;
    StreetAddress hiStreetAddress;

    public StreetAddressRange(StreetAddress loStreetAddress, StreetAddress hiStreetAddress)
    {
        this.loStreetAddress = loStreetAddress;
        this.hiStreetAddress = hiStreetAddress;
    }

}
