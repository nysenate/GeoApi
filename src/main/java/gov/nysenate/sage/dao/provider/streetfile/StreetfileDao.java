package gov.nysenate.sage.dao.provider.streetfile;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.DistrictedStreetRange;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.util.NonnullList;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface StreetfileDao {
    String nullString();

    List<DistrictType> order();

    void replaceStreetfile(Path streetfilePath) throws SQLException, IOException;

    /**
     * Performs a lookup in the streetfile table, consolidating districts if needed.
     * @param matchLevel the highest DistrictMatchLevel to attempt.
     * @return a districted address, with the highest possible match level.
     */
    DistrictedAddress getDistrictedAddress(Address addr, @Nonnull DistrictMatchLevel matchLevel);

    /**
     * Returns a list of street ranges with district information for a given zip5.
     * @return List of DistrictedStreetRange
     */
    List<DistrictedStreetRange> getDistrictStreetRangesByZip(Integer zip5);

    /**
     * Returns a list of street ranges with district information for a given street and zip5 list.
     * @return List of DistrictedStreetRange
     */
    List<DistrictedStreetRange> getDistrictStreetRanges(String street, List<Integer> zip5List);

    /**
     * Finds state district codes that overlap a given street/zip range.
     * @param streetList Optional street name
     * @param zip5List   Zip5 to match against
     * @return       A map of district types to a set of matched district codes.
     */
    Map<DistrictType, Set<String>> getAllStandardDistrictMatches(List<String> streetList, NonnullList<Integer> zip5List);

    /**
     * Finds state district codes that overlap a given street/zip range.
     * @param distType The district type to find intersections with
     * @param sourceId The id of that district
     * @return       A map of district types to a set of matched district codes.
     */
    Map<DistrictType, Set<String>> getAllIntersections(DistrictType distType, String sourceId);
}
