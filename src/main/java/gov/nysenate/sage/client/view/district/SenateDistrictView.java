package gov.nysenate.sage.client.view.district;

import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.district.SingleDistrict;
import gov.nysenate.services.model.Senator;

public class SenateDistrictView extends DistrictView {
    private final Senator senator;

    public SenateDistrictView(SingleDistrict data, DistrictType type, DistrictMap map, Senator senator) {
        super(data, type, map);
        this.senator = senator;
    }

    public Senator getSenator() {
        return senator;
    }
}
