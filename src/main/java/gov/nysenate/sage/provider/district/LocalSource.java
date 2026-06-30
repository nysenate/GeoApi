package gov.nysenate.sage.provider.district;

import gov.nysenate.sage.provider.DataSource;

public enum LocalSource implements DataSource {
    STREETFILE, SHAPEFILE;

    @Override
    public String getDisplayName() {
        return switch (this) {
            case STREETFILE -> "Board of Elections";
            case SHAPEFILE -> "LATFOR/GIS Geometry";
        };
    }
}
