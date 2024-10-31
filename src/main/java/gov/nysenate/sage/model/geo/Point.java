package gov.nysenate.sage.model.geo;

/**
 * Simple point implementation
 * @author Ken Zalewski
 * TODO: should be BigDecimal, or perhaps something latlon specific?
 */
public record Point(double lat, double lon) {}