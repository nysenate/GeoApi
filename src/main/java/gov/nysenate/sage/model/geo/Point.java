package gov.nysenate.sage.model.geo;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

/**
 * Simple point implementation
 * @author Ken Zalewski
 */
public record Point(BigDecimal lat, BigDecimal lon) {
    public Point(String lat, String lon) {
        this(new BigDecimal(lat), new BigDecimal(lon));
    }

    @Nonnull
    @Override
    public String toString() {
        return "(" + lat + " " + lon + ")";
    }
}