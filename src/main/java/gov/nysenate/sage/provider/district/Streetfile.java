package gov.nysenate.sage.provider.district;

import gov.nysenate.sage.dao.provider.streetfile.SqlStreetfileDao;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.DistrictedStreetRange;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.StreetAddressParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static gov.nysenate.sage.model.result.ResultStatus.INTERNAL_ERROR;
import static gov.nysenate.sage.model.result.ResultStatus.NO_DISTRICT_RESULT;
import static gov.nysenate.sage.service.district.DistrictServiceValidator.validateDistrictInfo;
import static gov.nysenate.sage.service.district.DistrictServiceValidator.validateInput;

/**
 * Streetfiles can come from many sources, and map addresses to districts.
 * This class aims to quickly match addresses to the database.
 */
@Service
public class Streetfile extends DistrictService implements StreetLookupService {
    private static final Logger logger = LoggerFactory.getLogger(Streetfile.class);
    private final SqlStreetfileDao sqlStreetFileDao;

    @Autowired
    public Streetfile(SqlStreetfileDao sqlStreetFileDao) {
        this.sqlStreetFileDao = sqlStreetFileDao;
    }

    /** {@inheritDoc} */
    @Override
    public List<DistrictedStreetRange> streetLookup(String zip5) {
        try {
            return sqlStreetFileDao.getDistrictStreetRangesByZip(zip5);
        }
        catch (NumberFormatException ex) {
            logger.error("Zip5 was not valid. Possible non-NY entry.", ex);
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress, List<DistrictType> reqTypes) {
        var districtResult = new DistrictResult(this.getClass());
        if (!validateInput(geocodedAddress, districtResult, false, true)) {
            return districtResult;
        }
        StreetAddress streetAddr = StreetAddressParser.parseAddress(geocodedAddress.getAddress());
        if (logger.isTraceEnabled()) {
            logger.trace("Streetfile lookup on " + streetAddr.toStringParsed());
        }

        try {
            DistrictedAddress match = sqlStreetFileDao.getDistrictedAddress(geocodedAddress.getAddress(), DistrictMatchLevel.HOUSE);
            if (match == null) {
                districtResult.setStatusCode(NO_DISTRICT_RESULT);
            }
            else {
                validateDistrictInfo(match.getDistrictInfo(), reqTypes, districtResult);
                districtResult.setDistrictedAddress(match);
            }
        } catch (Exception ex) {
            districtResult.setStatusCode(INTERNAL_ERROR);
            logger.error("" + ex);
        }

        districtResult.setResultTime(new Timestamp(new Date().getTime()));
        if (districtResult.getGeocodedAddress() != null) {
            logger.debug(FormatUtil.toJsonString(districtResult.getGeocodedAddress()));
        }
        else {
            logger.debug("The geocoded address was null");
        }
        return districtResult;
    }
}
