package gov.nysenate.sage.client.view.district;

import gov.nysenate.services.model.Senator;

public class SenateDistrictView extends DistrictView {
    protected Senator senator;

    public SenateDistrictView(DistrictView baseView, Senator senator) {
        super(baseView.getName(), baseView.getDistrict(), baseView.getMap());
        this.senator = senator;
    }

    public Senator getSenator() {
        return senator;
    }
}
