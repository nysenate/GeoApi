package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.api.BatchGeocodeRequest;
import gov.nysenate.sage.model.api.SingleGeocodeRequest;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.Geocoder;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

public interface GeocodeServiceProvider {
    /**
     * Performs single geocode using GeocodeRequest.
     * @param geocodeRequest GeocodeRequest object with geocode options and address set.
     * @return GeocodeResult
     */
    default GeocodeResult geocode(@Nonnull SingleGeocodeRequest geocodeRequest) {
        return geocode(geocodeRequest.getAddress(), geocodeRequest.getGeocoders(), geocodeRequest.isDoNotCache());
    }

    /**
     * Perform a single geocode with all specified parameters.
     * @param address           Address to geocode
     * @param doNotCache        If we shouldn't cache result
     * @return                  GeocodeResult
     */
    GeocodeResult geocode(Address address, List<Geocoder> providers, boolean doNotCache);

    /**
     * Perform batch geocoding using supplied BatchGeocodeRequest
     * @param batchGeoRequest BatchGeocodeRequest with desired fields set
     * @return  List<GeocodeResult> corresponding to the addresses list
     */
    default List<GeocodeResult> geocode(@Nonnull BatchGeocodeRequest batchGeoRequest) {
        return geocode(batchGeoRequest.getAddresses(), batchGeoRequest.getGeocoders(), batchGeoRequest.isDoNotCache());
    }

    /**
     * Perform a single geocode with all specified parameters.
     * @param addresses         Addresses to geocode
     * @param doNotCache        If we shouldn't cache result
     * @return                  GeocodeResult
     */
    List<GeocodeResult> geocode(List<Address> addresses, List<Geocoder> providers, boolean doNotCache);

    /**
     * Return a map containing a Geocoder and its service.
     * @return
     */
    Set<Geocoder> geocoders();
}
