package gov.nysenate.sage.client.response.street;

import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.view.street.StreetRangeView;
import gov.nysenate.sage.model.address.DistrictedStreetRange;
import gov.nysenate.sage.model.result.StreetResult;

import java.util.ArrayList;
import java.util.List;

public class StreetResponse extends BaseResponse {
    protected List<StreetRangeView> streets = new ArrayList<>();

    public StreetResponse(StreetResult streetResult) {
        super(streetResult);
        if (streetResult != null && streetResult.isSuccess()) {
            for (DistrictedStreetRange dsr : streetResult.getDistrictedStreetRanges()) {
                streets.add(new StreetRangeView(dsr));
            }
        }
    }

    public List<StreetRangeView> getStreets() {
        return streets;
    }
}
