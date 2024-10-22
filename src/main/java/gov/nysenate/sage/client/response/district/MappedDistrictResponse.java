package gov.nysenate.sage.client.response.district;

import gov.nysenate.sage.client.view.district.MappedDistrictsView;
import gov.nysenate.sage.model.result.DistrictResult;

public class MappedDistrictResponse extends BaseDistrictResponse {
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
