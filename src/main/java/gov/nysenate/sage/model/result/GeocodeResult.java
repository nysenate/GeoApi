package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.provider.geocode.Geocoder;

import java.util.List;

public class GeocodeResult extends BaseResult<Geocoder> {
    private GeocodedAddress geocodedAddress;

    public GeocodeResult(ResultStatus resultStatus) {
        this(null, resultStatus, null);
    }

    public GeocodeResult(Geocoder geocoder, ResultStatus resultStatus, GeocodedAddress geocodedAddress) {
        super(geocoder, resultStatus);
        this.geocodedAddress = geocodedAddress;
    }

    public GeocodeResult(List<Geocoder> geocoders, GeocodedAddress geocodedAddress) {
        super(geocoders, ResultStatus.SUCCESS);
        this.geocodedAddress = geocodedAddress;
    }

    /** Convenience accessor */
    public Geocode getGeocode() {
        return (geocodedAddress != null) ? geocodedAddress.getGeocode() : null;
    }

    public Address getAddress() {
        return (geocodedAddress != null) ? geocodedAddress.getAddress() : null;
    }

    public GeocodedAddress getGeocodedAddress() {
        return geocodedAddress;
    }

    public void setAddress(Address address) {
        if (geocodedAddress == null) {
            this.geocodedAddress = new GeocodedAddress(address);
        }
        else {
            geocodedAddress.setAddress(address);
        }
    }
}
