package gov.nysenate.sage.model.address;

import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;
import gov.nysenate.sage.scripts.streetfinder.model.StreetParity;

/**
 * Represents a range of street addresses
 */
public record StreetAddressRange(int bldgLow, int bldgHigh, StreetParity parity, AddressWithoutNum awn) {
    public StreetAddressRange(int bldgLow, int bldgHigh, String parity, AddressWithoutNum awn) {
        this(bldgLow, bldgHigh, StreetParity.getParityFromWord(parity), awn);
    }
}
