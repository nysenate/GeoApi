package gov.nysenate.sage.client.response.district;

import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.view.address.AddressView;
import gov.nysenate.sage.client.view.geo.GeocodeView;
import gov.nysenate.sage.client.view.district.MappedDistrictsView;
import gov.nysenate.sage.model.result.DistrictResult;

public class MappedDistrictResponse extends BaseResponse
{
    protected AddressView address;
    protected GeocodeView geocode;
    protected MappedDistrictsView districts;
    protected boolean geocoded;
    protected boolean districtAssigned;

    public MappedDistrictResponse(DistrictResult districtResult) {
        super(districtResult);
        if (districtResult != null) {
            if (districtResult.isSuccess() || districtResult.isPartialSuccess()) {
                this.districts = new MappedDistrictsView(districtResult.getDistrictInfo());
                this.districtAssigned = true;
            }
            if (districtResult.getAddress() != null) {
                this.address = new AddressView(districtResult.getAddress());
            }
            if (districtResult.getGeocode() != null) {
                this.geocode = new GeocodeView(districtResult.getGeocode());
                if (this.geocode != null) {
                    this.geocoded = true;
                }
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
}
