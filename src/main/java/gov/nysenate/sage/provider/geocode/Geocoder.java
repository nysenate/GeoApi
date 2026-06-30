package gov.nysenate.sage.provider.geocode;

import gov.nysenate.sage.provider.DataSource;

public enum Geocoder implements DataSource {
    // Note that GEOCACHE geocodes ultimately come from one of the other two sources.
    GEOCACHE, NYSGEO, GOOGLE;

    @Override
    public String getDisplayName() {
        return name();
    }
}
