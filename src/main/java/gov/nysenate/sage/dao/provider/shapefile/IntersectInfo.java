package gov.nysenate.sage.dao.provider.shapefile;

import java.math.BigDecimal;

public record IntersectInfo(String code, String geoJson, BigDecimal area) {}
