package gov.nysenate.sage.dao.provider.streetfile;

import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.DistrictedStreetRange;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.address.StreetAddressRange;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictType;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface StreetfileDao {
    /**
     * Performs a street file lookup.
     * @param streetAddr     The StreetAddress to base the search on
     * @param useStreet      Use the street name as a criteria for the search
     * @param fuzzy          Use a wildcard on the street name to expand the search space
     * @param useHouse       Use the house number as a criteria for the search
     * @return               A LinkedHashMap containing StreetAddressRange and DistrictInfo
     * @throws SQLException
     */
    Map<StreetAddressRange, DistrictInfo> getDistrictStreetRangeMap(
            StreetAddress streetAddr, boolean useStreet, boolean fuzzy, boolean useHouse) throws SQLException;

    /**
     * Returns a list of street ranges with district information for a given zip5.
     * @param zip5
     * @return List of DistrictedStreetRange
     */
    List<DistrictedStreetRange> getDistrictStreetRangesByZip(String zip5);

    /**
     * Returns a list of street ranges with district information for a given street and zip5 list.
     * @param street
     * @param zip5List
     * @return List of DistrictedStreetRange
     */
    List<DistrictedStreetRange> getDistrictStreetRanges(String street, List<String> zip5List);

    /**
     * Finds state district codes that overlap a given street/zip range.
     * @param streetList Optional street name
     * @param zip5List   Zip5 to match against
     * @return       A map of district types to a set of matched district codes.
     */
    Map<DistrictType, Set<String>> getAllStandardDistrictMatches(List<String> streetList, List<String> zip5List);

    /**
     * Finds state district codes that overlap a given street/zip range.
     * @param distType The district type to find intersections with
     * @param sourceId The id of that district
     * @return       A map of district types to a set of matched district codes.
     */
    Map<DistrictType, Set<String>> getAllIntersections(DistrictType distType, String sourceId);
    /**
     * Retreives a districted address by a house in th a street address
     * @param streetAddress
     * @return
     * @throws SQLException
     */
    DistrictedAddress getDistAddressByHouse(StreetAddress streetAddress) throws SQLException;

    /**
     * Retrieves a districted address by a street in a street address
     * @param streetAddress
     * @return
     * @throws SQLException
     */
    DistrictedAddress getDistAddressByStreet(StreetAddress streetAddress) throws SQLException;

    /**
     * Retrieves a districted address object from a zip code in a street address
     * @param streetAddress
     * @return
     * @throws SQLException
     */
    DistrictedAddress getDistAddressByZip(StreetAddress streetAddress) throws SQLException;
}
