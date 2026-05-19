package gov.nysenate.sage.client.response.district;

import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.ResultStatus;

public class DistrictTypeResponse extends BaseResponse {
    private final DistrictType type;

    public DistrictTypeResponse(DistrictType type) {
        super(ResultStatus.SUCCESS);
        this.type = type;
    }

    public String getEnumName() {
        return type.name();
    }

    public String getDisplayName() {
        return type.getDisplayName();
    }
}
