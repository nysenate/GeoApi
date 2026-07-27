package gov.nysenate.sage.service.district;

import gov.nysenate.sage.dao.provider.DistrictNameDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Caches the display names of every district, keyed by type and code.
 */
@Service
public class DistrictNameCache extends DistrictCodeCache<String> {
    @Autowired
    public DistrictNameCache(DistrictNameDao nameDao) {
        super(nameDao::getNameMap);
    }
}
