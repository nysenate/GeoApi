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

public interface StreetFileDao {
    /**
     * Performs a street file lookup.
     * @param streetAddr     The StreetAddress to base the search on
     * @param useStreet      Use the street name as a criteria for the search
     * @param fuzzy          Use a wildcard on the street name to expand the search space
     * @param useHouse       Use the house number as a criteria for the search
     * @return               A LinkedHashMap containing StreetAddressRange and DistrictInfo
     * @throws SQLException
     */
    public Map<StreetAddressRange, DistrictInfo> getDistrictStreetRangeMap(
            StreetAddress streetAddr, boolean useStreet, boolean fuzzy, boolean useHouse) throws SQLException;

    /**
     * Returns a list of street ranges with district information for a given zip5.
     * @param zip5
     * @return List of DistrictedStreetRange
     */
    public List<DistrictedStreetRange> getDistrictStreetRangesByZip(String zip5);

    /**
     * Returns a list of street ranges with district information for a given street and zip5 list.
     * @param street
     * @param zip5List
     * @return List of DistrictedStreetRange
     */
    public List<DistrictedStreetRange> getDistrictStreetRanges(String street, List<String> zip5List);

    /**
     * Retrieves the district matches based off of the zip5
     * @param zip5
     * @return
     */
    public Map<DistrictType, Set<String>> getAllStandardDistrictMatches(String zip5);

    /**
     * Retrieves the district matches based off of a street and a zip code
     * @param street
     * @param zip5
     * @return
     */
    public Map<DistrictType, Set<String>> getAllStandardDistrictMatches(String street, String zip5);

    /**
     * Retrieves the district matches for a list of zip 5's
     * @param zip5
     * @return
     */
    public Map<DistrictType, Set<String>> getAllStandardDistrictMatches(List<String> zip5);

    /**
     * Finds state district codes that overlap a given street/zip range.
     * @param streetList Optional street name
     * @param zip5List   Zip5 to match against
     * @return       A map of district types to a set of matched district codes.
     */
    public Map<DistrictType, Set<String>> getAllStandardDistrictMatches(List<String> streetList, List<String> zip5List);

    /**
     * Retreives a districted address by a house in th a street address
     * @param streetAddress
     * @return
     * @throws SQLException
     */
    public DistrictedAddress getDistAddressByHouse(StreetAddress streetAddress) throws SQLException;

    /**
     * Retrieves a districted address by a street in a street address
     * @param streetAddress
     * @return
     * @throws SQLException
     */
    public DistrictedAddress getDistAddressByStreet(StreetAddress streetAddress) throws SQLException;

    /**
     * Retrieves a districted address object from a zip code in a street address
     * @param streetAddress
     * @return
     * @throws SQLException
     */
    public DistrictedAddress getDistAddressByZip(StreetAddress streetAddress) throws SQLException;

    /**
     * Iterates over a list of DistrictInfo and returns a single DistrictInfo that represents the districts
     * that were common amongst every entry.
     * @param districtInfoList
     * @return DistrictInfo containing the districts that were common.
     *         If the senate code is not common, the return value will be null.
     */
    public DistrictInfo consolidateDistrictInfo(List<DistrictInfo> districtInfoList);


}
