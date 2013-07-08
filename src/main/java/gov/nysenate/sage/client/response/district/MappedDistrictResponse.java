package gov.nysenate.sage.client.response.district;

import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.view.address.AddressView;
import gov.nysenate.sage.client.view.geo.GeocodeView;
import gov.nysenate.sage.client.view.district.MappedDistrictsView;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.util.FormatUtil;

public class MappedDistrictResponse extends BaseResponse
{
    protected AddressView address;
    protected GeocodeView geocode;
    protected MappedDistrictsView districts;
    protected boolean geocoded = false;
    protected boolean districtAssigned = false;
    protected Boolean isMultiMatch = false;
    protected String matchLevel;

    public MappedDistrictResponse(DistrictResult districtResult) {
        super(districtResult);
        if (districtResult != null) {
            if (districtResult.isSuccess() || districtResult.isPartialSuccess()) {
                this.districts = new MappedDistrictsView(districtResult.getDistrictInfo());
                if (!districtResult.getAssignedDistricts().isEmpty()) {
                    this.districtAssigned = true;
                }
            }
            if (districtResult.getAddress() != null) {
                this.address = new AddressView(districtResult.getAddress());
            }
            if (districtResult.getGeocode() != null) {
                this.geocode = new GeocodeView(districtResult.getGeocode());
                if (districtResult.getGeocodedAddress().isValidGeocode()) {
                    this.geocoded = true;
                }
            }
            if (districtResult.getDistrictMatchLevel() != null) {
                this.matchLevel = districtResult.getDistrictMatchLevel().name();
            }
        }
    }

    public AddressView getAddress() {
        return address;
    }

    public GeocodeView getGeocode() {
        return geocode;
    }

    public MappedDistrictsView getDistricts() {
        return districts;
    }

    public boolean isGeocoded() {
        return geocoded;
    }

    public boolean isDistrictAssigned() {
        return districtAssigned;
    }

    public String getMatchLevel() {
        return matchLevel;
    }

    public Boolean getMultiMatch() {
        return isMultiMatch;
    }
}
