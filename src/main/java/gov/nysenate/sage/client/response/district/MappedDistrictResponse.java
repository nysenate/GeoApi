package gov.nysenate.sage.client.response.district;

import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.view.address.AddressView;
import gov.nysenate.sage.client.view.geo.GeocodeView;
import gov.nysenate.sage.client.view.district.MappedDistrictsView;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.util.FormatUtil;

public class MappedDistrictResponse extends BaseDistrictResponse
{
    protected MappedDistrictsView districts;

    public MappedDistrictResponse(DistrictResult districtResult) {
        super(districtResult);
        if (districtResult != null) {
            this.districts = new MappedDistrictsView(districtResult.getDistrictInfo());
        }
    }

    public MappedDistrictsView getDistricts() {
        return districts;
    }
}
