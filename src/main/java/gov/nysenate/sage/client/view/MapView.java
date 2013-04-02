package gov.nysenate.sage.client.view;

import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.geo.Polygon;
import gov.nysenate.sage.util.FormatUtil;

import java.util.ArrayList;
import java.util.List;

public class MapView
{
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
                        p[0] = point.getLat();
                        p[1] = point.getLon();
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
