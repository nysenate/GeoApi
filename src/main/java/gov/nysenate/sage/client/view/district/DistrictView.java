package gov.nysenate.sage.client.view.district;

import gov.nysenate.sage.client.view.map.PolygonMapView;
import gov.nysenate.sage.util.FormatUtil;

public class DistrictView {
    private final String name;
    private final String district;
    private final PolygonMapView map;

    public DistrictView(String name, String code, PolygonMapView mapView) {
        this.name = name;
        this.district = code;
        this.map = mapView;
    }

    public String getName() {
        return name;
    }

    public String getDistrict() {
        if (district != null && (district.isEmpty() || FormatUtil.trimLeadingZeroes(district).equals("0"))) {
            return null;
        }
        return district;
    }

    public PolygonMapView getMap() {
        return map;
    }
}
