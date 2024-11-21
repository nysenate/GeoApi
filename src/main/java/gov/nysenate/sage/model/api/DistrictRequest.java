package gov.nysenate.sage.model.api;

import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.provider.district.DistrictSource;
import gov.nysenate.sage.provider.geocode.Geocoder;
import gov.nysenate.sage.util.TimeUtil;

import java.sql.Timestamp;
import java.util.List;

/**
 * A DistrictRequest represents a district assignment API request.
 * It is intended to encapsulate the various options and input types.
 */
public abstract class DistrictRequest {
    /** The ids are assigned once the request has been logged */
    private int id;

    /** Geocoded Input */
    private GeocodedAddress geocodedAddress;

    /** DistrictTypes to assign */
    private List<DistrictType> districtTypes = DistrictType.getStandardTypes();

    /** District assign api options */
    protected List<DistrictSource> providers = List.of(DistrictSource.STREETFILE, DistrictSource.SHAPEFILE);
    protected Geocoder geocoder = null;
    protected boolean uspsValidate = false;
    protected boolean usePunct = false;
    protected boolean skipGeocode = false;
    private final Timestamp requestTime = TimeUtil.currentTimestamp();

    public DistrictRequest() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public GeocodedAddress getGeocodedAddress() {
        return geocodedAddress;
    }

    public void setGeocodedAddress(GeocodedAddress geocodedAddress) {
        this.geocodedAddress = geocodedAddress;
    }

    public List<DistrictType> getDistrictTypes() {
        return districtTypes;
    }

    public List<DistrictSource> getProviders() {
        return providers;
    }

    public void setProviders(List<DistrictSource> providers) {
        this.providers = providers;
    }

    public Geocoder getGeoProvider() {
        return geocoder;
    }

    public void setGeocoder(Geocoder geocoder) {
        this.geocoder = geocoder;
    }

    public boolean isUspsValidate() {
        return uspsValidate;
    }

    public void setUspsValidate(boolean uspsValidate) {
        this.uspsValidate = uspsValidate;
    }

    public boolean isUsePunct() {
        return usePunct;
    }

    public void setUsePunct(boolean usePunct) {
        this.usePunct = usePunct;
    }

    public boolean isSkipGeocode() {
        return skipGeocode;
    }

    public void setSkipGeocode(boolean skipGeocode) {
        this.skipGeocode = skipGeocode;
    }

    public void setDistrictTypes(List<DistrictType> districtTypes) {
        this.districtTypes = districtTypes;
    }

    public Timestamp getRequestTime() {
        return requestTime;
    }
}
