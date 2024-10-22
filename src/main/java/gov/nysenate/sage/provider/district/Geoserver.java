package gov.nysenate.sage.provider.district;

import gov.nysenate.sage.dao.provider.geoserver.HttpGeoserverDao;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This class is an implementation of DistrictService to perform district assignment given a
 * geocoded address. GeoServer is a WMS/WFS server that provides an API to retrieve geo-spatial
 * and feature information from source data such as Census Shapefiles. Note that a coordinate pair
 * is required to perform district assignment using this implementation.
 */
@Service
public class Geoserver extends DistrictService {
    private static final Logger logger = LoggerFactory.getLogger(Geoserver.class);
    private final HttpGeoserverDao httpGeoserverDao;

    @Autowired
    public Geoserver(HttpGeoserverDao httpGeoserverDao) {
        this.httpGeoserverDao = httpGeoserverDao;
    }

    @Override
    public DistrictSource districtSource() {
        return DistrictSource.GEOSERVER;
    }

    /** {@inheritDoc} */
    @Override
    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress, List<DistrictType> reqTypes) {
        var districtResult = new DistrictResult(districtSource(), geocodedAddress, true, false);
        if (!districtResult.isSuccess()) {
            return districtResult;
        }
        DistrictInfo districtInfo = httpGeoserverDao.getDistrictInfo(geocodedAddress.getGeocode().point(), reqTypes);
        // The quality here is always point since it's based off a geocode
        districtResult.setDistrictedAddress(new DistrictedAddress(geocodedAddress, districtInfo, DistrictMatchLevel.HOUSE));
        return districtResult;
    }
}
