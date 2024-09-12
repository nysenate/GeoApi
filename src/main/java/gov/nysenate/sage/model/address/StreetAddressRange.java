package gov.nysenate.sage.model.address;

import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;

/**
 * Represents a range of street addresses
 */
public record StreetAddressRange(int bldgLow, int bldgHigh, String parity, AddressWithoutNum awn) {}
