package gov.nysenate.sage.client.view.district;

import gov.nysenate.sage.client.view.map.PolygonMapView;

public class DistrictView {
    private final String name;
    private final String district;
    private final String displayName;
    private final PolygonMapView map;

    public DistrictView(String name, String code, String displayName, PolygonMapView mapView) {
        this.name = name;
        this.district = code;
        this.displayName = displayName;
        this.map = mapView;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDistrict() {
        if (district == null || district.isBlank()) {
            return null;
        }
        return district;
    }

    public PolygonMapView getMap() {
        return map;
    }
}
