package gov.nysenate.sage.client.response;

import gov.nysenate.sage.client.response.base.BatchResponse;
import gov.nysenate.sage.scripts.streetfinder.NamePair;

import java.util.List;

public class BatchNamePairResponse extends BatchResponse<NamePair> {
    public BatchNamePairResponse(List<NamePair> pairs) {
        this.results = pairs;
    }
}
