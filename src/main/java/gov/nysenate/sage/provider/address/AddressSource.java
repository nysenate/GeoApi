package gov.nysenate.sage.provider.address;

import gov.nysenate.sage.provider.DataSource;

/**
 * These two sources are ultimately both USPS.
 */
public enum AddressSource implements DataSource {
    AMS, AIS;

    @Override
    public String getDisplayName() {
        return name();
    }
}
