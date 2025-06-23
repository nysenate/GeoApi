package gov.nysenate.sage.client.response.district;

import gov.nysenate.sage.client.response.base.BatchResponse;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.result.DistrictResultWithMembers;
import gov.nysenate.sage.util.Tuple;

import java.util.ArrayList;
import java.util.List;

public class BatchDistrictResponse extends BatchResponse<DistrictResponse> {
    private BatchDistrictResponse(List<Tuple<DistrictResultWithMembers, GeocodedAddress>> results) {
        super(results, result -> new DistrictResponse(result.first(), result.second(), false));
    }

    public static BatchDistrictResponse of(List<DistrictResultWithMembers> districtResults, List<GeocodedAddress> geoAddrs) {
        var tupleList = new ArrayList<Tuple<DistrictResultWithMembers, GeocodedAddress>>();
        for (int i = 0; i < districtResults.size(); i++) {
            tupleList.add(new Tuple<>(districtResults.get(i), geoAddrs.get(i)));
        }
        return new BatchDistrictResponse(tupleList);
    }
}
