package gov.nysenate.sage.model.district;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a mapping of districts that overlap a given reference area. Typically this is used
 * to determine which senate districts overlap a set of zipcode regions. However any form of overlap
 * amongst district types can be represented here.
 */
public class DistrictOverlap
{
    public enum AreaUnit {
        SQ_FEET, SQ_METERS, SQ_MILES, SQ_DEGREES
    }

    /** If computing which senate districts overlap a given zip, ZIP would be the reference type. */
    protected DistrictType referenceType;

    /** If computing which senate districts overlap a given zip, SENATE would be the target type. */
    protected DistrictType targetType;

    /** The list of codes that are used as the total area to compute overlap on. For example this
     * could be a list of zip codes that represent a city. */
    protected Set<String> referenceCodes;

    /** A map of `targetType` district codes along with an approx percentage of how much it overlaps the total area */
    protected Map<String, BigDecimal> targetOverlap = new HashMap<>();

    /** The unit of measurement for the totalArea */
    protected AreaUnit areaUnit;

    /** The number of `areaUnits` that encompass the union of the areas of all the reference codes */
    protected Double totalArea;

    public DistrictOverlap() {}

}
