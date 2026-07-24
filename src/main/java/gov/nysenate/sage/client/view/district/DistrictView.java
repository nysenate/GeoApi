package gov.nysenate.sage.client.view.district;

import gov.nysenate.sage.model.district.*;

public class DistrictView extends BaseDistrictView {
    public DistrictView(DistrictType type, String code) {
        super(type, code);
    }

    public String getDisplayName() {
        return type.getDisplayName();
    }
}
