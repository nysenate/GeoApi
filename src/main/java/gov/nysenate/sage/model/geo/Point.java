package gov.nysenate.sage.model.geo;

/**
 * Simple point implementation
 * @author Ken Zalewski
 */
public record Point(double lat, double lon) {
    public Point(String lat, String lon) {
        this(Double.parseDouble(lat), Double.parseDouble(lon));
    }
}