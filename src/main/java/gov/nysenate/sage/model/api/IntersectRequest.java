package gov.nysenate.sage.model.api;

import gov.nysenate.sage.model.district.DistrictType;

public record IntersectRequest(ApiRequest request, DistrictType sourceType,
                               String sourceId, DistrictType intersectWith) {}
