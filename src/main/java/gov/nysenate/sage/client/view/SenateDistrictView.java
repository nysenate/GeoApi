package gov.nysenate.sage.client.view;

import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.services.model.Senator;

public class SenateDistrictView extends DistrictView
{
    protected Senator senator;

    public SenateDistrictView(DistrictInfo districtInfo, Senator senator) {
        super(DistrictType.SENATE, districtInfo);
        this.senator = senator;
    }

    public Senator getSenator() {
        return senator;
    }
}
