package gov.nysenate.sage.model.address;

import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;
import gov.nysenate.sage.scripts.streetfinder.model.StreetParity;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a range of street addresses
 */
public record StreetAddressRange(int bldgLow, int bldgHigh, StreetParity parity, AddressWithoutNum awn) {
    public StreetAddressRange(int bldgLow, int bldgHigh, String parity, AddressWithoutNum awn) {
        this(bldgLow, bldgHigh, StreetParity.getParityFromWord(parity), awn);
    }

    public List<Address> addresses() {
        var addresses = new ArrayList<Address>();
        for (int num = bldgLow; num <= bldgHigh;) {
            addresses.add(new Address(num, awn));
            if (parity == StreetParity.ALL) {
                num++;
            }
            else {
                num += 2;
            }
        }
        return addresses;
    }
}
