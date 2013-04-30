package gov.nysenate.sage.client.view;

import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.services.model.Senator;

public class MappedSenateDistrictView extends MappedDistrictView
{
    protected Senator senator;

    public MappedSenateDistrictView(DistrictInfo districtInfo, Senator senator) {
        super(DistrictType.SENATE, districtInfo);
        this.senator = senator;
    }

    public Senator getSenator() {
        return senator;
    }
}
