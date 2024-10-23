package gov.nysenate.sage.client.view.map;

import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.geo.Line;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.geo.Polygon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PolygonMapView
{
    protected int precision = 8;
    protected List<List<Double[]>> geom;
    protected String type = "";

    public PolygonMapView(DistrictMap districtMap)
    {
        if (districtMap != null && districtMap.getPolygons() != null) {
            this.type = districtMap.getGeometryType();
            this.geom = new ArrayList<>();
            for (Polygon polygon : districtMap.getPolygons()) {
                if (polygon.getPoints() != null) {
                    List<Double[]> geomPoly = new ArrayList<>();
                    for (Point point : polygon.getPoints()) {
                        Double[] p = new Double[2];
                        BigDecimal lat = BigDecimal.valueOf(point.lat());
                        BigDecimal lon = BigDecimal.valueOf(point.lon());
                        p[0] = lat.setScale(this.precision, BigDecimal.ROUND_HALF_UP).doubleValue();
                        p[1] = lon.setScale(this.precision, BigDecimal.ROUND_HALF_UP).doubleValue();
                        geomPoly.add(p);
                    }
                    this.geom.add(geomPoly);
                }
            }
        }
    }

    public PolygonMapView(List<Line> lines)
    {
        if (lines != null && !lines.isEmpty())  {
            this.geom = new ArrayList<>();
            for (Line line : lines) {
                if (line.getPoints() != null) {
                    List<Double[]> geomPoly = new ArrayList<>();
                    for (Point point : line.getPoints()) {
                        Double[] p = new Double[2];
                        BigDecimal lat = BigDecimal.valueOf(point.lat());
                        BigDecimal lon = BigDecimal.valueOf(point.lon());
                        p[0] = lat.setScale(this.precision, BigDecimal.ROUND_HALF_UP).doubleValue();
                        p[1] = lon.setScale(this.precision, BigDecimal.ROUND_HALF_UP).doubleValue();
                        geomPoly.add(p);
                    }
                    this.geom.add(geomPoly);
                }
            }
        }
    }

    public List<List<Double[]>> getGeom()
    {
        return geom;
    }

    public String getType() {
        return type;
    }
}
