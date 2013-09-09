package gov.nysenate.sage.client.response.district;

import gov.nysenate.sage.client.view.district.DistrictsView;
import gov.nysenate.sage.model.result.DistrictResult;

public class DistrictResponse extends BaseDistrictResponse
{
    protected DistrictsView districts;

    public DistrictResponse(DistrictResult districtResult)
    {
        super(districtResult);
        if (districtResult != null) {
            this.districts = new DistrictsView(districtResult.getDistrictInfo());
        }
    }

    public DistrictsView getDistricts() {
        return districts;
    }
}
