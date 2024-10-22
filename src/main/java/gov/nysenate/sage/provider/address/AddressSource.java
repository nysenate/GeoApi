package gov.nysenate.sage.provider.address;

import gov.nysenate.sage.provider.geocode.DataSource;

public enum AddressSource implements DataSource {
    AMS, AIS;

    public static AddressSource fromString(String value) {
        value = value.toUpperCase().trim();
        if (value.matches("(?i)AMS|usps")) {
            return AMS;
        }
        if (value.matches("(?i)AIS|uspsais")) {
            return AIS;
        }
        return null;
    }
}
