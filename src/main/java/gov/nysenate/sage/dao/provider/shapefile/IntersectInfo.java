package gov.nysenate.sage.dao.provider.shapefile;

import gov.nysenate.sage.model.district.DistrictId;

import java.math.BigDecimal;

public record IntersectInfo(DistrictId id, String geoJson, BigDecimal area) {}
