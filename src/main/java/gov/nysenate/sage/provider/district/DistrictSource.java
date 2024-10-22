package gov.nysenate.sage.provider.district;

import gov.nysenate.sage.provider.geocode.DataSource;

public enum DistrictSource implements DataSource {
    SHAPEFILE, STREETFILE, GEOSERVER
}
