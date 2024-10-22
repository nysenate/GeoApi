package gov.nysenate.sage.provider.geocode;

import gov.nysenate.sage.dao.provider.nysgeo.HttpNYSGeoDao;
import gov.nysenate.sage.service.geo.ParallelGeocodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class NYSGeocoder extends GeocodeService {
    @Autowired
    public NYSGeocoder(HttpNYSGeoDao httpNysGeoDao, ParallelGeocodeService parallelGeocodeService) {
        super(httpNysGeoDao, parallelGeocodeService);
    }

    @Nonnull
    @Override
    public Geocoder name() {
        return Geocoder.NYSGEO;
    }
}
