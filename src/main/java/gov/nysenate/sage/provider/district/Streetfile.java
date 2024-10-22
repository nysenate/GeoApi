package gov.nysenate.sage.provider.district;

import gov.nysenate.sage.dao.provider.streetfile.StreetfileDao;
import gov.nysenate.sage.model.address.DistrictedStreetRange;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.service.street.StreetData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * Streetfiles can come from many sources, and map addresses to districts.
 * This class aims to quickly match addresses to the database.
 */
@Service
public class Streetfile extends DistrictService implements StreetLookupService {
    private static final Logger logger = LoggerFactory.getLogger(Streetfile.class);
    private final StreetfileDao streetfileDao;

    @Autowired
    public Streetfile(StreetfileDao streetfileDao) {
        this.streetfileDao = streetfileDao;
    }

    @Override
    public DistrictSource districtSource() {
        return DistrictSource.STREETFILE;
    }

    /** {@inheritDoc} */
    @Override
    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress, List<DistrictType> reqTypes) {
        var districtResult = new DistrictResult(districtSource(), geocodedAddress, false, true);
        if (!districtResult.isSuccess()) {
            return districtResult;
        }
        districtResult.setDistrictedAddress(
                streetfileDao.getDistrictedAddress(geocodedAddress.getAddress(), DistrictMatchLevel.HOUSE)
        );
        districtResult.setResultTime(new Timestamp(new Date().getTime()));
        return districtResult;
    }

    @Override
    public StreetData source() {
        return StreetData.STREETFILE;
    }

    /** {@inheritDoc} */
    @Override
    public List<DistrictedStreetRange> streetLookup(Integer zip5) {
        try {
            return streetfileDao.getDistrictStreetRangesByZip(zip5);
        }
        catch (NumberFormatException ex) {
            logger.error("Zip5 was not valid. Possible non-NY entry.", ex);
            return null;
        }
    }
}
