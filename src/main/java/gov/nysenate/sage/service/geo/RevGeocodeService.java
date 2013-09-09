package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;

import java.util.ArrayList;

/**
* Base interface for providers of reverse geocoding services.
*/
public interface RevGeocodeService
{
    public GeocodeResult reverseGeocode(Point point);
    public ArrayList<GeocodeResult> reverseGeocode(ArrayList<Point> points);
}
