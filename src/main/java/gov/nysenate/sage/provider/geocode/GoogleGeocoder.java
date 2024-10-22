package gov.nysenate.sage.provider.geocode;

import gov.nysenate.sage.dao.provider.google.HttpGoogleDao;
import gov.nysenate.sage.service.geo.ParallelGeocodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

@Service
public class GoogleGeocoder extends GeocodeService implements RevGeocodeService {
    @Autowired
    public GoogleGeocoder(HttpGoogleDao httpGoogleDao, ParallelGeocodeService parallelGeocodeService) {
        super(httpGoogleDao, parallelGeocodeService);
    }

    @Nonnull
    @Override
    public Geocoder name() {
        return Geocoder.GOOGLE;
    }
}
