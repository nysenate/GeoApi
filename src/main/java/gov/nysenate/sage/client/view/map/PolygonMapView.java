package gov.nysenate.sage.client.view.map;

import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.geo.Polygon;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class PolygonMapView {
    private static final int precision = 8;
    protected List<List<Double[]>> geom;
    protected String type;

    public PolygonMapView(List<Polygon> polygons, String type) {
        this.type = type;
        if (polygons == null) {
            return;
        }
        this.geom = new ArrayList<>();
        for (Polygon polygon : polygons) {
            if (polygon.getPoints() != null) {
                List<Double[]> geomPoly = new ArrayList<>();
                for (Point point : polygon.getPoints()) {
                    Double[] p = new Double[2];
                    p[0] = point.lat().setScale(precision, RoundingMode.HALF_UP).doubleValue();
                    p[1] = point.lon().setScale(precision, RoundingMode.HALF_UP).doubleValue();
                    geomPoly.add(p);
                }
                this.geom.add(geomPoly);
            }
        }
    }

    public PolygonMapView(DistrictMap districtMap) {
        this(districtMap.getPolygons(), districtMap.getGeometryType());
    }

    public List<List<Double[]>> getGeom() {
        return geom;
    }

    public String getType() {
        return type;
    }
}
