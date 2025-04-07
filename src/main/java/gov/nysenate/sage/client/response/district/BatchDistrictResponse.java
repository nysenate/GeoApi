package gov.nysenate.sage.client.response.district;

import gov.nysenate.sage.client.response.base.BatchResponse;
import gov.nysenate.sage.model.result.DistrictResultWithMembers;

import java.util.List;

public class BatchDistrictResponse extends BatchResponse<DistrictResponse> {
    public BatchDistrictResponse(List<DistrictResultWithMembers> districtResults) {
        super(districtResults, DistrictResponse::new);
    }
}
