package gov.nysenate.sage.client.view;

import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.geo.Point;

import java.util.ArrayList;
import java.util.List;

public class MapView
{
    protected List<Double[]> geom;

    public MapView(DistrictMap districtMap)
    {
        if (districtMap != null && districtMap.getPolygon() != null) {
            if (districtMap.getPolygon().getPoints() != null) {

                /** Create new list to hold coordinates */
                geom = new ArrayList<>();

                /** Convert each point to a simple array and add to the point list */
                for (Point point : districtMap.getPolygon().getPoints()) {
                    Double[] p = new Double[2];
                    p[0] = point.getLat();
                    p[1] = point.getLon();
                    geom.add(p);
                }
            }
        }
    }

    public List<Double[]> getGeom()
    {
        return geom;
    }
}
