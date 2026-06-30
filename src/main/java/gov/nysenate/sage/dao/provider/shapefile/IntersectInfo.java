package gov.nysenate.sage.dao.provider.shapefile;

import gov.nysenate.sage.model.geo.Polygon;

import java.math.BigDecimal;
import java.util.List;

public record IntersectInfo(String code, List<Polygon> polygons, BigDecimal area) {}
