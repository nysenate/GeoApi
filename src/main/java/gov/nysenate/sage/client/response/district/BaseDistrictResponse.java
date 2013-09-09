package gov.nysenate.sage.client.response.district;

import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.view.address.AddressView;
import gov.nysenate.sage.client.view.geo.GeocodeView;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;

public abstract class BaseDistrictResponse extends BaseResponse
{
    protected AddressView address;
    protected GeocodeView geocode;
    protected boolean geocoded = false;
    protected boolean districtAssigned = false;
    protected boolean senateAssigned = false;
    protected boolean uspsValidated = false;
    protected Boolean isMultiMatch = false;
    protected String matchLevel;

    public BaseDistrictResponse(DistrictResult districtResult)
    {
        super(districtResult);
        if (districtResult != null) {

            this.districtAssigned = !districtResult.getAssignedDistricts().isEmpty();
            this.senateAssigned = districtResult.getAssignedDistricts().contains(DistrictType.SENATE);
            this.matchLevel = districtResult.getDistrictMatchLevel().name();
            this.isMultiMatch = districtResult.isMultiMatch();

            GeocodedAddress geocodedAddress = districtResult.getGeocodedAddress();
            if (geocodedAddress != null) {
                if (geocodedAddress.getAddress() != null) {
                    this.address = new AddressView(districtResult.getAddress());
                }
                if (geocodedAddress.getGeocode() != null) {
                    this.geocode = new GeocodeView(districtResult.getGeocode());
                    if (geocodedAddress.isValidGeocode()) {
                        this.geocoded = true;
                    }
                }
            }
            this.uspsValidated = districtResult.isUspsValidated();
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
}