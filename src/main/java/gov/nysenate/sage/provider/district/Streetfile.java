package gov.nysenate.sage.provider.district;

import gov.nysenate.sage.dao.provider.streetfile.StreetfileDao;
import gov.nysenate.sage.model.address.DistrictedStreetRange;
import gov.nysenate.sage.model.address.Zip5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Streetfiles can come from many sources, and map addresses to districts.
 * This class aims to quickly match addresses to the database.
 */
@Service
public class Streetfile implements StreetLookupService {
    private static final Logger logger = LoggerFactory.getLogger(Streetfile.class);
    private final StreetfileDao streetfileDao;

    @Autowired
    public Streetfile(StreetfileDao streetfileDao) {
        this.streetfileDao = streetfileDao;
    }

    /** {@inheritDoc} */
    @Override
    public List<DistrictedStreetRange> streetLookup(Zip5 zip5) {
        try {
            return streetfileDao.getDistrictStreetRangesByZip(zip5);
        }
        catch (NumberFormatException ex) {
            logger.error("Zip5 was not valid. Possible non-NY entry.", ex);
            return null;
        }
    }
}
