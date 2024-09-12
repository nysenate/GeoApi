package gov.nysenate.sage.provider.geocode;

import gov.nysenate.sage.dao.provider.tiger.SqlTigerGeocoderDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.address.GeocodedStreetAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.geo.GeocodeServiceValidator;
import gov.nysenate.sage.service.geo.ParallelGeocodeService;
import gov.nysenate.sage.util.StreetAddressParser;
import gov.nysenate.sage.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * TigerGeocoder is a geocoding implementation that resides as a collection of functions
 * within the PostGIS database. The geocoder automatically performs address parsing and
 * implements fuzzy matching. This service primarily relays requests to the TigerGeocoderDao
 * and performs validation and result formatting.
 */
@Service
public class TigerGeocoder implements GeocodeService, RevGeocodeService {
    private static final Logger logger = LoggerFactory.getLogger(TigerGeocoder.class);
    private final SqlTigerGeocoderDao sqlTigerGeocoderDao;
    private final GeocodeServiceValidator geocodeServiceValidator;
    private final ParallelGeocodeService parallelGeocodeService;

    @Autowired
    public TigerGeocoder(SqlTigerGeocoderDao sqlTigerGeocoderDao, GeocodeServiceValidator geocodeServiceValidator,
                         ParallelGeocodeService parallelGeocodeService) {
        this.sqlTigerGeocoderDao = sqlTigerGeocoderDao;
        this.geocodeServiceValidator = geocodeServiceValidator;
        this.parallelGeocodeService = parallelGeocodeService;
        logger.debug("Instantiated TigerGeocoder.");
    }

    /** {@inheritDoc} */
    @Override
    public GeocodeResult geocode(Address address) {
        GeocodeResult geocodeResult = new GeocodeResult(this.getClass());
        if (address == null) {
            geocodeResult.setStatusCode(ResultStatus.MISSING_ADDRESS);
            geocodeResult.setResultTime(TimeUtil.currentTimestamp());
        }
        else {
            logger.debug("Performing geocoding using TigerGeocoder for address {}", address);

            /** Ensure that the geocoder is active, otherwise return error result. */
            if (!geocodeServiceValidator.isGeocodeServiceActive(this.getClass(), geocodeResult)) {
                return geocodeResult;
            }

            /** Proceed if valid address */
            if (!GeocodeServiceValidator.validateGeocodeInput(address, geocodeResult)){
                return geocodeResult;
            }

            /** Retrieve geocoded addresses from dao */
            GeocodedStreetAddress gsa = sqlTigerGeocoderDao.getGeocodedStreetAddress(address);

            if (gsa != null) {
                Geocode geocode = gsa.getGeocode();
                StreetAddress streetAddress = gsa.getStreetAddress();
                Address convertedAddress = streetAddress.toAddress();
                geocode.setQuality(resolveGeocodeQuality(address, gsa));
                GeocodedAddress geocodedAddress = new GeocodedAddress(convertedAddress, geocode);

                geocodeServiceValidator.validateGeocodeResult(this.getClass(), geocodedAddress, geocodeResult, false);
            }
            else {
                geocodeResult.setStatusCode(ResultStatus.NO_GEOCODE_RESULT);
                geocodeResult.setResultTime(TimeUtil.currentTimestamp());
            }
        }
        return geocodeResult;
    }

    /** {@inheritDoc} */
    @Override
    public List<GeocodeResult> geocode(List<Address> addresses) {
        return parallelGeocodeService.geocode(this, addresses);
    }

    /** {@inheritDoc} */
    @Override
    public GeocodeResult reverseGeocode(Point point) {
        GeocodeResult geocodeResult = new GeocodeResult(this.getClass());
        StreetAddress streetAddress = sqlTigerGeocoderDao.getStreetAddress(point);
        if (streetAddress != null && streetAddress.hasStreet()) {
            Address address = streetAddress.toAddress();
            Geocode geocode = new Geocode(point);
            GeocodedAddress geocodedAddress = new GeocodedAddress(address, geocode);
            geocodeResult.setGeocodedAddress(geocodedAddress);
        }
        else {
            geocodeResult.setStatusCode(ResultStatus.NO_REVERSE_GEOCODE_RESULT);
        }
        return geocodeResult;
    }

    /**
     * TigerGeocoder provides more of a confidence rating than a quality rating. For example a geocode
     * that has a high rating ( low ratings are better ) might turn out to be accurate but the
     * geocoder is not sure because it had to fill in some of the address fields. This method resolves
     * the ratings into basic quality codes by taking into account the address the results are based off of.
     *
     * @param inputAddress  The original address used for geocoding
     * @param gsa           The geo street address returned by the TigerGeocoderDao
     * @return              Resolved GeocodeQuality
     */
    private static GeocodeQuality resolveGeocodeQuality(Address inputAddress, GeocodedStreetAddress gsa) {
        int rawQuality = gsa.getGeocode().getRawQuality();
        StreetAddress sa = gsa.getStreetAddress();

        StreetAddress isa = StreetAddressParser.parseAddress(inputAddress);

        /*
        If the raw quality is 0, then it is an exact match
         */
        if (rawQuality == 0) {
            return GeocodeQuality.POINT;
        }

        //The following comparisons are based on the Address object inputAddress

        // A matching building number usually means it's a house quality match. Also, we don't want to
        // perform any zipcode corrections for house level matches. If the zipcodes don't match it is
        // not a house level match.
        if (sa.getBldgId() != null && inputAddress.getAddr1().contains(sa.getBldgId())) {
            if (inputAddress.getZip5() == null || inputAddress.getZip5().equals(sa.getZip5())){
                return GeocodeQuality.HOUSE;
            }
        }

        // If the zip code matches, we can check to see if the streets match. If they don't match, we
        // can't be sure if it's the correct street or not so it becomes a zip level geocode.
        if (inputAddress.getZip5() != null && inputAddress.getZip5().equals(sa.getZip5()) ) {
            if (inputAddress.getAddr1().contains(sa.getStreet()) && rawQuality <= 60 ) {
                return GeocodeQuality.STREET;
            }
            return GeocodeQuality.ZIP;
        }

        // Check to see if the cities match
        if (inputAddress.getPostalCity().equalsIgnoreCase(sa.getPostalCity())) {
            return GeocodeQuality.CITY;
        }

        // The following comparisons are based on the StreetAddress object isa. This is necessary because an address
        // given to tiger might not all be in the addr field. To compare specifics for a slit apart addr, it is
        // converted to an StreetAddress
        if (sa.getBldgId() != null && Objects.equals(isa.getBldgId(), sa.getBldgId())) {
            if (isa.getZip5() == null || isa.getZip5().equals(sa.getZip5())) {
                return GeocodeQuality.HOUSE;
            }
        }

        if (isa.getZip5() != null && isa.getZip5().equals( sa.getZip5() ) ) {
            if (isa.getStreet().contains(sa.getStreet()) && rawQuality <= 60 ) {
                return GeocodeQuality.STREET;
            }
            return GeocodeQuality.ZIP;
        }

        if (isa.getPostalCity().equalsIgnoreCase(sa.getPostalCity())) {
            return GeocodeQuality.CITY;
        }

        // Failed to determine if the geocode is accurate, so it's safer to return no match
        return GeocodeQuality.NOMATCH;
    }
}
