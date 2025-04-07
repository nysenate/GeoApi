package gov.nysenate.sage.client.response.district;

import gov.nysenate.sage.client.view.district.DistrictsView;
import gov.nysenate.sage.model.result.DistrictResultWithMembers;

public class DistrictResponse extends BaseDistrictResponse {
    protected DistrictsView districts;

    public DistrictResponse(DistrictResultWithMembers districtResult) {
        super(districtResult);
        this.districts = new DistrictsView(districtResult);
    }

    public DistrictsView getDistricts() {
        return districts;
    }
}
