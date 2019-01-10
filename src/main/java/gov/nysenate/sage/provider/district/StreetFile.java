package gov.nysenate.sage.provider.district;

import gov.nysenate.sage.dao.provider.streetfile.SqlStreetFileDao;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.DistrictedStreetRange;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.service.district.ParallelDistrictService;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.StreetAddressParser;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static gov.nysenate.sage.model.result.ResultStatus.*;
import static gov.nysenate.sage.service.district.DistrictServiceValidator.validateDistrictInfo;
import static gov.nysenate.sage.service.district.DistrictServiceValidator.validateInput;

/**
 * A street file provider implementation to resolve district codes.
   Street files are distributed by the Board of Elections on a county basis.
   These files contain address ranges with corresponding district code information.
   District information can be obtained quickly by matching a given address to an
   address range stored in the street file database.
 */
@Service
public class StreetFile implements DistrictService, StreetLookupService
{
    private Logger logger = LoggerFactory.getLogger(StreetFile.class);
    private SqlStreetFileDao sqlStreetFileDao;
    private ParallelDistrictService parallelDistrictService;

    @Autowired
    public StreetFile(SqlStreetFileDao sqlStreetFileDao, ParallelDistrictService parallelDistrictService) {
        this.sqlStreetFileDao = sqlStreetFileDao;
        this.parallelDistrictService = parallelDistrictService;
        logger.debug("Instantiated StreetFile.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresGeocode() { return false; }

    /** {@inheritDoc} */
    @Override
    public List<DistrictedStreetRange> streetLookup(String zip5)
    {
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
    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress)
    {
        return assignDistricts(geocodedAddress, DistrictType.getStateBasedTypes());
    }

    /** {@inheritDoc} */
    @Override
    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress, List<DistrictType> reqTypes)
    {
        DistrictResult districtResult = new DistrictResult(this.getClass());

        /** Validate input */
        if (!validateInput(geocodedAddress, districtResult, false, true)) {
            return districtResult;
        }
        /** Parse the address */
        StreetAddress streetAddr = StreetAddressParser.parseAddress(geocodedAddress.getAddress());
        if (logger.isTraceEnabled()) {
            logger.trace("Streetfile lookup on " + streetAddr.toStringParsed());
        }

        try {
            DistrictedAddress match = null;

            if (!streetAddr.isStreetEmpty()) {
                /** Try a House level match */
                match = sqlStreetFileDao.getDistAddressByHouse(streetAddr);
                /** Try a Street level match */
                if (match == null) {
                    match = sqlStreetFileDao.getDistAddressByStreet(streetAddr);
                }
            }
            /** Try a Zip5 level match */
            if (match == null) {
                match = sqlStreetFileDao.getDistAddressByZip(streetAddr);
            }

            /** Validate result and return error status */
            if (match == null) {
                districtResult.setStatusCode(NO_DISTRICT_RESULT);
            }
            else {
                validateDistrictInfo(match.getDistrictInfo(), reqTypes, districtResult);
                districtResult.setDistrictedAddress(match);
            }
        }
        catch (SQLException ex) {
            districtResult.setStatusCode(DATABASE_ERROR);
            logger.error("" + ex);
        }
        catch (Exception ex) {
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

    /** {@inheritDoc} */
    @Override
    public DistrictResult assignDistrictsForBatch(GeocodedAddress geocodedAddress, List<DistrictType> reqTypes)
    {
        return assignDistricts(geocodedAddress, reqTypes);
    }

    /** {@inheritDoc} */
    @Override
    public List<DistrictResult> assignDistricts(List<GeocodedAddress> geocodedAddresses)
    {
        return parallelDistrictService.assignDistricts(this, geocodedAddresses, DistrictType.getStateBasedTypes());
    }

    /** {@inheritDoc} */
    @Override
    public List<DistrictResult> assignDistricts(List<GeocodedAddress> geocodedAddresses, List<DistrictType> types)
    {
        return parallelDistrictService.assignDistricts(this, geocodedAddresses, types);
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, DistrictMap> nearbyDistricts(GeocodedAddress geocodedAddress, DistrictType districtType)
    {
        logger.warn("Nearby district search is not implemented using streetfiles!");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, DistrictMap> nearbyDistricts(GeocodedAddress geocodedAddress, DistrictType districtType, int count)
    {
        logger.warn("Nearby district search is not implemented using streetfiles!");
        return null;
    }
}