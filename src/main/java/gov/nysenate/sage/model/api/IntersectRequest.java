package gov.nysenate.sage.model.api;

import gov.nysenate.sage.model.district.DistrictType;

public record IntersectRequest(DistrictType sourceType, String sourceId, DistrictType intersectWith) {}
