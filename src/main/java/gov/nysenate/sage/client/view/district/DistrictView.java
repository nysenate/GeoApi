package gov.nysenate.sage.client.view.district;

import gov.nysenate.sage.model.district.*;

public class DistrictView extends BaseDistrictView {
    public DistrictView(DistrictType type, DistrictId id) {
        super(type, id);
    }

    public String getDisplayName() {
        return type.getDisplayName();
    }
}
