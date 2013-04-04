package gov.nysenate.sage.client.response;

import gov.nysenate.sage.client.response.BaseResponse;
import gov.nysenate.sage.client.view.AddressView;
import gov.nysenate.sage.client.view.DistrictsView;
import gov.nysenate.sage.client.view.GeocodeView;
import gov.nysenate.sage.model.result.DistrictResult;

import java.util.List;

public class DistrictResponse extends BaseResponse
{
    protected AddressView address;
    protected GeocodeView geocode;
    protected DistrictsView districts;
    protected boolean geocoded;
    protected boolean districtAssigned;

    public DistrictResponse(DistrictResult districtResult)
    {
        super(districtResult);
        if (districtResult != null) {
            if (districtResult.isSuccess() || districtResult.isPartialSuccess()) {
                this.districts = new DistrictsView(districtResult.getDistrictInfo());
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

    public DistrictsView getDistricts() {
        return districts;
    }

    public boolean isGeocoded() {
        return geocoded;
    }

    public boolean isDistrictAssigned() {
        return districtAssigned;
    }
}
