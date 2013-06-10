package gov.nysenate.sage.client.response;

import gov.nysenate.sage.model.result.DistrictResult;

import java.util.ArrayList;
import java.util.List;

public class BatchDistrictResponse extends BatchResponse<DistrictResponse>
{
    public BatchDistrictResponse(List<DistrictResult> districtResults)
    {
        super();
        List<DistrictResponse> responses = new ArrayList<>();
        for (DistrictResult districtResult : districtResults) {
            responses.add(new DistrictResponse(districtResult));
        }
        this.results = responses;
    }
}
