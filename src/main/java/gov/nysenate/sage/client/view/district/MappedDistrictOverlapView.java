package gov.nysenate.sage.client.view.district;

import gov.nysenate.sage.client.view.map.PolygonMapView;
import gov.nysenate.sage.model.district.DistrictMap;

import javax.annotation.Nonnull;

public class MappedDistrictOverlapView extends DistrictOverlapView {
    protected PolygonMapView map;
    protected PolygonMapView fullMap;

    public MappedDistrictOverlapView(@Nonnull DistrictMap baseMap, @Nonnull DistrictMap intersectionMap) {
        super(baseMap, intersectionMap);
        this.map = new PolygonMapView(intersectionMap);
        this.fullMap = new PolygonMapView(baseMap);
    }

    public PolygonMapView getMap() {
        return map;
    }

    public PolygonMapView getFullMap() {
        return fullMap;
    }
}