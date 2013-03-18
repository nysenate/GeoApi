package gov.nysenate.sage.provider;

import gov.nysenate.sage.dao.provider.TigerGeocoderDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.address.GeocodedStreetAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.geo.GeocodeService;
import gov.nysenate.sage.service.geo.ParallelGeocodeService;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * TigerGeocoder is a geocoding implementation that resides as a collection of functions
 * within the PostGIS database. The geocoder automatically performs address parsing and
 * implements fuzzy matching. This service primarily relays requests to the TigerGeocoderDao
 * and performs validation and result formatting.
 */
public class TigerGeocoder implements GeocodeService
{
    private static Logger logger = Logger.getLogger(TigerGeocoder.class);
    private TigerGeocoderDao tigerGeocoderDao;

    public TigerGeocoder()
    {
        this.tigerGeocoderDao = new TigerGeocoderDao();
    }

    /**
     * Geocode a single address using TigerGeocoder.
     * @param address   Address to geocode
     * @return          GeocodeResult
     */
    @Override
    public GeocodeResult geocode(Address address)
    {
        GeocodeResult geocodeResult = new GeocodeResult(this.getClass());

        /** Proceed if valid address */
        if (!validateRequest(address, geocodeResult)){
            return geocodeResult;
        }

        GeocodedStreetAddress gsa = tigerGeocoderDao.getGeocodedStreetAddress(address);

        if (gsa != null) {
            Geocode geocode = gsa.getGeocode();

            StreetAddress streetAddress = gsa.getStreetAddress();
            Address convertedAddress = streetAddress.toAddress();
            geocode.setQuality(resolveGeocodeQuality(address, gsa));

            GeocodedAddress geocodedAddress = new GeocodedAddress(convertedAddress, geocode);
            geocodeResult.setGeocodedAddress(geocodedAddress);

            /** Return success result only if the GeocodeQuality indicates a match */
            if (geocode.getQuality().equals(GeocodeQuality.NOMATCH)) {
                geocodeResult.setStatusCode(ResultStatus.NO_GEOCODE_RESULT);
            }
        }

        return geocodeResult;
    }

    @Override
    public ArrayList<GeocodeResult> geocode(ArrayList<Address> addresses)
    {
        return ParallelGeocodeService.geocode(this, addresses);
    }

    @Override
    public GeocodeResult reverseGeocode(Point point)
    {
        GeocodeResult geocodeResult = new GeocodeResult(this.getClass());
        StreetAddress streetAddress = tigerGeocoderDao.getStreetAddress(point);
        if (streetAddress != null && !streetAddress.isEmpty()){
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
     * Validates the input and performs basic null checks.
     *
     * @param address        The input address
     * @param geocodeResult  A reference to the geocode result
     * @return               True if input valid, otherwise false and the geocodeResult
     *                       will have the appropriate status set.
     */
    private boolean validateRequest(Address address, GeocodeResult geocodeResult)
    {
        if (address == null) {
            geocodeResult.setStatusCode(ResultStatus.MISSING_ADDRESS);
            return false;
        }
        else if (address.isEmpty()) {
            geocodeResult.setStatusCode(ResultStatus.INSUFFICIENT_ADDRESS);
            return false;
        }
        return true;
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
    private GeocodeQuality resolveGeocodeQuality(Address inputAddress, GeocodedStreetAddress gsa)
    {
        int rawQuality = gsa.getGeocode().getRawQuality();
        StreetAddress sa = gsa.getStreetAddress();

        /** A matching building number usually means its a house quality match. */
        if (sa.getBldgNum() != 0 && inputAddress.getAddr1().contains(Integer.toString(sa.getBldgNum()))) {
            return GeocodeQuality.HOUSE;
        }

        /** If the zip code matches, we can check to see if the streets match. If they don't match, we
         * can't be sure if it's the correct street or not so it becomes a zip level geocode. */
        if (!inputAddress.getZip5().isEmpty() && inputAddress.getZip5() == sa.getZip5()) {
            if (inputAddress.getAddr1().contains(sa.getStreet()) && rawQuality <= 60 ) { return GeocodeQuality.STREET; }
            return GeocodeQuality.ZIP;
        }

        /** Check to see if the cities match */
        if (inputAddress.getCity().equalsIgnoreCase(sa.getLocation())) { return GeocodeQuality.CITY; }

        /** Failed to determine if the geocode is accurate, so it's safer to return no match */
        return GeocodeQuality.NOMATCH;
    }
}
