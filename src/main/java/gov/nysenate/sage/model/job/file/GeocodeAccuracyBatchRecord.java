package gov.nysenate.sage.model.job.file;

import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.util.GeocodeUtil;

public class GeocodeAccuracyBatchRecord extends GeocodeBatchRecord
{
    protected Double refLat;
    protected Double refLon;
    protected String refGeoMethod;
    protected String refQuality;
    protected Double distance;

    public void setReferenceGeocode(Geocode refGeocode)
    {
        if (refGeocode != null) {
            this.refLat = refGeocode.getLat();
            this.refLon = refGeocode.getLon();
            this.refGeoMethod = refGeocode.getMethod();
            this.refQuality = refGeocode.getQuality().name();
        }
    }

    public Double getRefLat() {
        return refLat;
    }

    public void setRefLat(Double refLat) {
        this.refLat = refLat;
    }

    public Double getRefLon() {
        return refLon;
    }

    public void setRefLon(Double refLon) {
        this.refLon = refLon;
    }

    public String getRefGeoMethod() {
        return refGeoMethod;
    }

    public void setRefGeoMethod(String refGeoMethod) {
        this.refGeoMethod = refGeoMethod;
    }

    public String getRefQuality() {
        return refQuality;
    }

    public void setRefQuality(String refQuality) {
        this.refQuality = refQuality;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }
}
