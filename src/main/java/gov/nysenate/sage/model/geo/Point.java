package gov.nysenate.sage.model.geo;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

/**
 * Simple point implementation.
 */
public record Point(BigDecimal lat, BigDecimal lon) {
    public Point(String lat, String lon) {
        this(new BigDecimal(lat), new BigDecimal(lon));
    }

    public boolean isValid() {
        return lat != null && lon != null && !lat.equals(BigDecimal.ZERO) && !lon.equals(BigDecimal.ZERO);
    }

    @Nonnull
    @Override
    public String toString() {
        return "(" + lat + " " + lon + ")";
    }
}