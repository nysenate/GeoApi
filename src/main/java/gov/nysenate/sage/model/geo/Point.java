package gov.nysenate.sage.model.geo;

import java.math.BigDecimal;

/**
 * Simple point implementation
 * @author Ken Zalewski
 * TODO: should be BigDecimal, or perhaps something latlon specific?
 */
public record Point(BigDecimal lat, BigDecimal lon) {
    public Point(String lat, String lon) {
        this(new BigDecimal(lat), new BigDecimal(lon));
    }

    public String specificToString() {
        return "(" + lat + " " + lon + ")";
    }
}