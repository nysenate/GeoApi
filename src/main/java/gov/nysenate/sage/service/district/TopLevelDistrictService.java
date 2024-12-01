package gov.nysenate.sage.service.district;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.api.*;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.district.DistrictSource;
import gov.nysenate.sage.service.address.AddressServiceProvider;
import gov.nysenate.sage.service.geo.GeocodeServiceProvider;
import gov.nysenate.sage.service.geo.RevGeocodeServiceProvider;
import gov.nysenate.sage.service.geo.SageGeocodeServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static gov.nysenate.sage.model.result.ResultStatus.*;

@Service
public class TopLevelDistrictService {
    private static final Logger logger = LoggerFactory.getLogger(TopLevelDistrictService.class);

    private final AddressServiceProvider addressProvider;
    private final DistrictServiceProvider districtProvider;
    private final GeocodeServiceProvider geocodeProvider;
    private final RevGeocodeServiceProvider revGeocodeProvider;

    @Autowired
    public TopLevelDistrictService(AddressServiceProvider addressProvider, DistrictServiceProvider districtProvider,
                                   SageGeocodeServiceProvider geocodeProvider, RevGeocodeServiceProvider revGeocodeProvider) {
        this.addressProvider = addressProvider;
        this.districtProvider = districtProvider;
        this.geocodeProvider = geocodeProvider;
        this.revGeocodeProvider = revGeocodeProvider;
    }

    /**
     * Handle district assignment requests and performs functions based on settings in the supplied DistrictRequest.
     *
     * @param districtRequest Contains the various parameters for the District Assign/Bluebird API
     * @return DistrictResult
     */
    public DistrictResult handleDistrictRequest(SingleDistrictRequest districtRequest) {
        Address address = districtRequest.getAddress();
        GeocodedAddress geocodedAddress = null;

        if (address != null && address.isValid()) {
            /* Perform USPS address correction if requested */
            if (districtRequest.isUspsValidate()) {
                address = performAddressCorrection(address, districtRequest);
            }
            geocodedAddress = getGeocodedAddress(districtRequest, address);
        }
        /* Perform reverse geocoding for point input */
        else if (districtRequest.getPoint() != null) {
            geocodedAddress = new GeocodedAddress(new Geocode(districtRequest.getPoint(), GeocodeQuality.POINT, "User Supplied"));
        } else {
            return new DistrictResult(null, geocodedAddress, MISSING_INPUT_PARAMS);
        }

        return performDistrictAssign(geocodedAddress, districtRequest.getProviders(),
                districtRequest.getDistrictTypes());
    }

    /**
     * Utilizes the service providers to perform batch address validation, geocoding, and district assignment for an address.
     * @return List<DistrictResult>
     */
    public List<DistrictResult> handleBatchDistrictRequest(BatchDistrictRequest batchRequest) {
        List<Point> points = batchRequest.getPoints();
        batchRequest.setAddresses(batchRequest.getAddresses());

        List<GeocodedAddress> geocodedAddresses;
        if (!batchRequest.getAddresses().isEmpty()) {
            geocodedAddresses = getGeocodedAddresses(batchRequest);
        }
        else if (!points.isEmpty()) {
            geocodedAddresses = points.stream().map(point -> new Geocode(point, GeocodeQuality.POINT, "User Supplied"))
                    .map(GeocodedAddress::new).toList();
            batchRequest.setProviders(List.of(DistrictSource.SHAPEFILE));
        }
        else {
            // No addresses and no points, nothing to do.
            logger.warn("No input for batch api request! Returning empty list.");
            return new ArrayList<>();
        }

        batchRequest.setGeocodedAddresses(geocodedAddresses);
        return districtProvider.assignDistricts(batchRequest);
    }

    private List<GeocodedAddress> getGeocodedAddresses(BatchDistrictRequest batchRequest) {
        List<Address> addresses = batchRequest.getAddresses();
        /* Batch USPS validation */
        if (batchRequest.isUspsValidate()) {
            List<AddressResult> validationResult = addressProvider.validate(addresses, null, batchRequest.isUsePunct());
            if (validationResult != null && validationResult.size() == addresses.size()) {
                addresses = validationResult.stream().map(AddressResult::getAddress).toList();
            }
        }

        if (batchRequest.isSkipGeocode()) {
            return addresses.stream().map(GeocodedAddress::new).toList();
        }
        BatchGeocodeRequest batchGeocodeRequest = new BatchGeocodeRequest(batchRequest);
        List<GeocodeResult> geocodeResults = geocodeProvider.geocode(batchGeocodeRequest);
        for (int i = 0; i < geocodeResults.size(); i++) {
            GeocodeResult geocodeResult = geocodeResults.get(i);
            Address currAddress = addresses.get(i);
            // Use Address if good, otherwise just keep geocoded address
            if (currAddress.isValid() && currAddress.isUspsValidated()) {
                geocodeResult.setAddress(currAddress);
            }
        }

        return geocodeResults.stream().map(GeocodeResult::getGeocodedAddress).toList();
    }

    /**
     * Perform USPS address correction on either the geocoded address or the input address.
     * If the geocoded address is invalid, the original address will be corrected and set as the address
     * on the supplied geocodedAddress parameter.
     *
     * @return GeocodedAddress the address corrected geocodedAddress.
     */
    private Address performAddressCorrection(Address address, DistrictRequest districtRequest) {
        boolean usePunct = districtRequest != null && districtRequest.isUsePunct();
        AddressResult addressResult = addressProvider.validate(address, null, usePunct);
        if (addressResult != null && addressResult.isValidated()) {
            var validatedAddress = addressResult.getAddress();
            if (validatedAddress != null && validatedAddress.isValid()) {
                validatedAddress.setUspsValidated(true);
                return validatedAddress;
            }
        }
        return address;
    }

    /**
     * Performs geocoding using the default geocode service provider.
     *
     * @param geoRequest The GeocodeRequest to handle.
     * @return GeocodedAddress
     */
    private GeocodedAddress performGeocode(SingleGeocodeRequest geoRequest) {
        GeocodeResult geocodeResult;

        /* Address-to-point geocoding */
        if (!geoRequest.isReverse()) {
            geocodeResult = geocodeProvider.geocode(geoRequest);
        }
        /* Point-to-address geocoding */
        else {
            geocodeResult = revGeocodeProvider.reverseGeocode(geoRequest);
        }

        return geocodeResult != null ? geocodeResult.getGeocodedAddress() : null;
    }

    private GeocodedAddress getGeocodedAddress(DistrictRequest districtRequest, Address address) {
        if (districtRequest.isSkipGeocode()) {
            return new GeocodedAddress(address);
        }
        var geocodeRequest = new SingleGeocodeRequest(address, districtRequest.getGeoProvider(), true, true);
        GeocodedAddress geocodedAddress = performGeocode(geocodeRequest);
        if (address.isUspsValidated() && geocodedAddress != null && geocodedAddress.isValidGeocode() &&
                (geocodedAddress.getGeocode().quality().compareTo(GeocodeQuality.HOUSE) >= 0)) {
            geocodedAddress = new GeocodedAddress(address, geocodedAddress.getGeocode());
            geocodedAddress.getAddress().setUspsValidated(true);
        }
        return geocodedAddress;
   }

    /**
     * Performs either single or multi-district assignment based on the quality of the geocode and the input address.
     * If either an address or geocode is missing, the method will set the appropriate error statuses to the DistrictResult.
     *
     * @return DistrictResult
     */
    private DistrictResult performDistrictAssign(@Nonnull GeocodedAddress geocodedAddress, List<DistrictSource> providers,
                                                 List<DistrictType> types) {
        if (geocodedAddress.isValidAddress()) {
            if (!geocodedAddress.isValidGeocode()) {
                return new DistrictResult(null, geocodedAddress, INVALID_GEOCODE);
            }
            return districtProvider.assignDistricts(geocodedAddress, providers, types);
        } else if (geocodedAddress.isValidGeocode()) {
            return districtProvider.assignDistricts(geocodedAddress, List.of(DistrictSource.SHAPEFILE), types);
        }
        return new DistrictResult(null, geocodedAddress, INSUFFICIENT_ADDRESS);
    }
}
