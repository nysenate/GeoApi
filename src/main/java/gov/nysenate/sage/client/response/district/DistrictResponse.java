package gov.nysenate.sage.client.response.district;

import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.view.address.AddressView;
import gov.nysenate.sage.client.view.district.DistrictsView;
import gov.nysenate.sage.client.view.geo.GeocodeView;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.result.DistrictResultWithMembers;

public class DistrictResponse extends BaseResponse {
    protected AddressView address;
    protected GeocodeView geocode;
    protected boolean geocoded = false;
    protected boolean districtAssigned = false;
    protected boolean senateAssigned = false;
    protected boolean uspsValidated = false;
    protected boolean isMultiMatch = false;
    protected String matchLevel;
    protected DistrictsView districts;

    public DistrictResponse(DistrictResultWithMembers districtResult, GeocodedAddress geoAddr) {
        super(districtResult);
        if (districtResult == null) {
            return;
        }
        this.districtAssigned = !districtResult.getAssignedDistricts().isEmpty();
        this.senateAssigned = districtResult.getAssignedDistricts().contains(DistrictType.SENATE);
        this.matchLevel = districtResult.getDistrictInfo().matchLevel().name();
        this.isMultiMatch = districtResult.isMultiMatch();
        this.districts = new DistrictsView(districtResult);
        if (geoAddr == null) {
            return;
        }
        Address realAddress = geoAddr.getAddress();
        if (realAddress != null) {
            this.address = new AddressView(realAddress);
            this.uspsValidated = realAddress.isUspsValidated();
        }
        Geocode realGeocode = geoAddr.getGeocode();
        if (realGeocode != null) {
            this.geocode = new GeocodeView(realGeocode);
            this.geocoded = realGeocode.isValidGeocode();
        }
    }

    public AddressView getAddress() {
        return address;
    }

    public GeocodeView getGeocode() {
        return geocode;
    }

    public boolean isGeocoded() {
        return geocoded;
    }

    public boolean isSenateAssigned() {
        return senateAssigned;
    }

    public boolean isDistrictAssigned() {
        return districtAssigned;
    }

    public boolean isUspsValidated() {
        return uspsValidated;
    }

    public String getMatchLevel() {
        return matchLevel;
    }

    public Boolean getMultiMatch() {
        return isMultiMatch;
    }

    public DistrictsView getDistricts() {
        return districts;
    }
}
