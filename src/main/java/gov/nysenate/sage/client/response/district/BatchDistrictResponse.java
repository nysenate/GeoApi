package gov.nysenate.sage.client.response.district;

import gov.nysenate.sage.client.response.base.BatchResponse;
import gov.nysenate.sage.model.result.DistrictResult;

import java.util.List;

public class BatchDistrictResponse extends BatchResponse<DistrictResponse> {
    public BatchDistrictResponse(List<DistrictResult> districtResults) {
        super(districtResults, DistrictResponse::new);
    }
}
