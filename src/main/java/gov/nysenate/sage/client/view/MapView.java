package gov.nysenate.sage.client.view;

import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.geo.Polygon;
import gov.nysenate.sage.util.FormatUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MapView
{
    protected int precision = 8;
    protected List<List<Double[]>> geom;

    public MapView(DistrictMap districtMap)
    {
        if (districtMap != null && districtMap.getPolygons() != null) {
            geom = new ArrayList<>();
            for (Polygon polygon : districtMap.getPolygons()) {
                if (polygon.getPoints() != null) {
                    List<Double[]> geomPoly = new ArrayList<>();
                    for (Point point : polygon.getPoints()) {
                        Double[] p = new Double[2];
                        BigDecimal lat = new BigDecimal(point.getLat());
                        BigDecimal lon = new BigDecimal(point.getLon());
                        p[0] = lat.setScale(precision, BigDecimal.ROUND_HALF_UP).doubleValue();
                        p[1] = lon.setScale(precision, BigDecimal.ROUND_HALF_UP).doubleValue();
                        geomPoly.add(p);
                    }
                    geom.add(geomPoly);
                }
            }
        }
    }

    public List<List<Double[]>> getGeom()
    {
        return geom;
    }
}
