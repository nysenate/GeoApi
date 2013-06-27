package gov.nysenate.sage.dao.provider;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.address.*;
import gov.nysenate.sage.model.district.DistrictInfo;
import static gov.nysenate.sage.model.district.DistrictType.*;

import static gov.nysenate.sage.model.district.DistrictQuality.*;
import gov.nysenate.sage.model.district.DistrictType;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class StreetFileDao extends BaseDao
{
    private Logger logger = Logger.getLogger(StreetFileDao.class);
    private QueryRunner run = getQueryRunner();

    private static Map<DistrictType, String> distColMap = new HashMap<>();
    static {
        distColMap.put(SENATE, "senate_code");
        distColMap.put(ASSEMBLY, "assembly_code");
        distColMap.put(CONGRESSIONAL, "congressional_code");
        distColMap.put(COUNTY, "county_code");
        distColMap.put(SCHOOL, "school_code");
        distColMap.put(TOWN, "town_code");
        distColMap.put(ELECTION, "election_code");
        distColMap.put(CLEG, "cleg_code");
        distColMap.put(CITY, "city_code");
        distColMap.put(FIRE, "fire_code");
        distColMap.put(VILLAGE, "vill_code");
        distColMap.put(WARD, "ward_code");
        distColMap.put(ZIP, "zip5");
    }

    public StreetFileDao() {}

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
            StreetAddress streetAddr, boolean useStreet, boolean fuzzy, boolean useHouse) throws SQLException
    {
        ArrayList<Object> params = new ArrayList<>();
        String sql = "SELECT * FROM streetfile WHERE 1=1 \n";

        boolean whereZip = (streetAddr.getZip5() != null && !streetAddr.getZip5().isEmpty());
        boolean whereState = (streetAddr.getState() != null && !streetAddr.getState().isEmpty());
        boolean whereStreet = (useStreet && streetAddr.getStreet() != null && !streetAddr.getStreet().isEmpty());
        boolean whereBldg = (useHouse && streetAddr.getBldgNum() != 0);
        boolean whereBldgChr = (useHouse && streetAddr.getBldgChar() != null && !streetAddr.getBldgChar().isEmpty());

        if (whereZip) {
            sql += " AND zip5=? \n";
            params.add(Integer.valueOf(streetAddr.getZip5()));
        }

        if (whereState) {
            sql += " AND state=? \n";
            params.add(streetAddr.getState());
        }

        if (whereStreet) {

            String street = (streetAddr.getPreDir() != null && !streetAddr.getPreDir().isEmpty()) ? streetAddr.getPreDir() + " " : "";
            street += streetAddr.getStreet();
            street += (streetAddr.getPostDir() != null && !streetAddr.getPostDir().isEmpty()) ? " " + streetAddr.getPostDir() : "";
            street = street.toUpperCase();

            /** Sometimes the bldg_chr is actually the tail end of the street name */
            if (whereBldgChr) {
                /** Handle dashed NYC buildings by collapsing on the dash */
                if (streetAddr.getBldgChar().startsWith("-")) {
                    try {
                        String bldgNum = String.valueOf(streetAddr.getBldgNum()) + streetAddr.getBldgChar().substring(1);
                        streetAddr.setBldgNum(Integer.parseInt(bldgNum));
                        streetAddr.setBldgChar(null);
                    }
                    catch (NumberFormatException e) {
                        logger.warn("bldg_chr `" + streetAddr.getBldgChar() + "` not as expected.");
                    }
                }

                /** Every one else gets a range check; sometimes the suffix is actually part of the street prefix. */
                if (streetAddr.getBldgChar() != null) {
                    if (fuzzy) {
                        sql += " AND (street LIKE ? OR (street LIKE ? AND (bldg_lo_chr='' OR bldg_lo_chr <= ?) AND (bldg_hi_chr='' OR ? <= bldg_hi_chr))) \n";
                        params.add(streetAddr.getBldgChar() + " " + street + "%");
                        params.add(street + "%");
                    }
                    else {
                        sql += " AND (street = ? OR (street = ? AND (bldg_lo_chr='' OR bldg_lo_chr <= ?) AND (bldg_hi_chr='' OR ? <= bldg_hi_chr))) \n";
                        params.add(streetAddr.getBldgChar() + " " + street);
                        params.add(street);
                    }
                    params.add(streetAddr.getBldgChar());
                    params.add(streetAddr.getBldgChar());
                }
            }
            else {
                /** Loose street match */
                if (fuzzy) {
                    sql += " AND (street LIKE ?) \n";
                    params.add(street + "%");
                }
                /** Strict street match */
                else {
                    street += (streetAddr.getStreetType() != null && !streetAddr.getStreetType().isEmpty()) ? streetAddr.getStreetType() : "";
                    street = street.toUpperCase();
                    sql += " AND (street = ?) \n";
                    params.add(street);
                }
            }

            if (whereBldg) {
                sql += " AND (bldg_lo_num <= ? AND ? <= bldg_hi_num AND (bldg_parity='ALL' or bldg_parity= "
                        + (streetAddr.getBldgNum() % 2 == 0 ? "'EVENS'" : "'ODDS'") + ")) \n";
                params.add(streetAddr.getBldgNum());
                params.add(streetAddr.getBldgNum());
            }
        }
        /** Only do a lookup if we have meaningful filters on the query */
        if (whereZip || whereStreet) {
            return run.query(sql, new DistrictStreetRangeMapHandler(), params.toArray());
        }
        else {
            logger.debug("Skipping address: no identifying information " + streetAddr);
            return null;
        }
    }

    /**
     * Returns a list of street ranges with district information for a given zip5.
     * @param zip5
     * @return List of DistrictedStreetRange
     */
    public List<DistrictedStreetRange> getDistrictStreetRangesByZip(int zip5)
    {
        String sql = "SELECT * FROM streetfile WHERE zip5 = ? ORDER BY street, bldg_lo_num";
        try {
            Map<StreetAddressRange, DistrictInfo> resultMap = run.query(sql, new DistrictStreetRangeMapHandler(), zip5);
            if (resultMap != null && resultMap.size() > 0) {
                List<DistrictedStreetRange> districtedStreetRanges = new ArrayList<>();
                for (StreetAddressRange sar : resultMap.keySet()) {
                    districtedStreetRanges.add(new DistrictedStreetRange(sar, resultMap.get(sar)));
                }
                return districtedStreetRanges;
            }
        }
        catch (SQLException ex) {
            logger.error("Failed to get district street range lookup!", ex);
        }
        return null;
    }

    /**
     * Finds state district codes that overlap a given street/zip range.
     * @param street Optional street name
     * @param zip5   Zip5 to match against
     * @return       A map of district types to a set of matched district codes.
     */
    public Map<DistrictType, Set<String>> getAllStateDistrictMatches(String street, String zip5)
    {
        street = (street == null) ? "" : StringEscapeUtils.escapeSql(street);
        zip5 = (zip5 == null ? "" : StringEscapeUtils.escapeSql(zip5));
        if (zip5.isEmpty()) return null;  // Short circuit on missing input
        String sqlTmpl = "SELECT DISTINCT %s AS code, '%s' AS type\n" +
                         "FROM streetfile\n" +
                         "WHERE zip5 = '%s' AND \n" +
                         "CASE WHEN '%s' != '' THEN street LIKE '%s%%' ELSE TRUE END";
        List<String> queryList = new ArrayList<>();
        for (DistrictType dType : DistrictType.getStateBasedTypes()) {
            String column = distColMap.get(dType);
            String type = dType.name();
            queryList.add(String.format(sqlTmpl, column, type, zip5, street, street));
        }
        String sqlQuery = StringUtils.join(queryList, " UNION ALL ");
        try {
            return run.query(sqlQuery, new ResultSetHandler<Map<DistrictType, Set<String>>>() {
                @Override
                public Map<DistrictType, Set<String>> handle(ResultSet rs) throws SQLException {
                    Map<DistrictType, Set<String>> resultMap = new HashMap<>();
                    while (rs.next()) {
                        DistrictType type = DistrictType.resolveType(rs.getString("type"));
                        String code = rs.getString("code");
                        if (!resultMap.containsKey(type)) {
                            resultMap.put(type, new HashSet<String>());
                        }
                        resultMap.get(type).add(code);
                    }
                    return resultMap;
                }
            });
        }
        catch (SQLException ex) {
            logger.error("Failed to get all possible state districts!", ex);
        }
        logger.info(sqlQuery);
        return null;
    }

    public DistrictedAddress getDistAddressByHouse(StreetAddress streetAddress) throws SQLException
    {
        return getDistAddressByHouse(streetAddress, false);
    }

    public DistrictedAddress getDistAddressByStreet(StreetAddress streetAddress) throws SQLException
    {
        return getDistAddressByStreet(streetAddress, false);
    }

    private DistrictedAddress getDistAddressByHouse(StreetAddress streetAddress, boolean useFuzzy) throws SQLException
    {
        Map<StreetAddressRange, DistrictInfo> rangeMap = getDistrictStreetRangeMap(streetAddress, true, useFuzzy, true);

        if (rangeMap == null ) {
            return null;
        }

        List<DistrictInfo> ranges = new ArrayList<>(rangeMap.values());
        DistrictInfo consolidatedDist = consolidateDistrictInfo(ranges);

        /** If the consolidated dist returned null, we can either recurse with fuzzy on or return null */
        if (consolidatedDist == null ) {
            if (!useFuzzy) {
                return getDistAddressByHouse(streetAddress, true);
            }
            else {
                return null;
            }
        }
        else {
            return new DistrictedAddress(new GeocodedAddress(streetAddress.toAddress()), consolidatedDist, HOUSE);
        }
    }

    private DistrictedAddress getDistAddressByStreet(StreetAddress streetAddress, boolean useFuzzy) throws SQLException
    {
        Map<StreetAddressRange, DistrictInfo> rangeMap = getDistrictStreetRangeMap(streetAddress, true, useFuzzy, false);

        if (rangeMap == null ) {
            return null;
        }

        List<DistrictInfo> ranges = new ArrayList<>(rangeMap.values());
        DistrictInfo consolidatedDist = consolidateDistrictInfo(ranges);

        /** If the consolidated dist returned null, we can either recurse with fuzzy on or return null */
        if (consolidatedDist == null ) {
            if (!useFuzzy) {
                return getDistAddressByStreet(streetAddress, true);
            }
            else {
                return null;
            }
        }
        else {
            return new DistrictedAddress(new GeocodedAddress(streetAddress.toAddress()), consolidatedDist, STREET);
        }
    }

    public DistrictedAddress getDistAddressByZip(StreetAddress streetAddress) throws SQLException
    {
        Map<StreetAddressRange, DistrictInfo> rangeMap = getDistrictStreetRangeMap(streetAddress, false, false, false);

        if (rangeMap == null ) {
            return null;
        }

        List<DistrictInfo> ranges = new ArrayList<>(rangeMap.values());
        DistrictInfo consolidatedDist = consolidateDistrictInfo(ranges);
        if (consolidatedDist != null){
            return new DistrictedAddress(new GeocodedAddress(streetAddress.toAddress()), consolidatedDist, ZIP5);
        }
        else {
            return null;
        }
    }

    /**
     * Iterates over a list of DistrictInfo and returns a single DistrictInfo that represents the districts
     * that were common amongst every entry.
     * @param districtInfoList
     * @return DistrictInfo containing the districts that were common.
     *         If the senate code is not common, the return value will be null.
     */
    public DistrictInfo consolidateDistrictInfo(List<DistrictInfo> districtInfoList)
    {
        if (districtInfoList.size() == 0) return null;
        DistrictInfo baseDist = districtInfoList.get(0);

        for (int i= 1; i < districtInfoList.size(); i++) {
            DistrictInfo rangeDist = districtInfoList.get(i);

            /** Iterate through all district types and ensure that the districts in the base range are consistent
             *  with the current range. If a district has a mismatch, then the district code in the base range is nullified.
             */
            for (DistrictType distType : DistrictType.getAllTypes()) {

                String baseCode = baseDist.getDistCode(distType);
                String baseCounty = baseDist.getDistCode(COUNTY);
                String baseTown = baseDist.getDistCode(TOWN);
                String rangeCode = rangeDist.getDistCode(distType);
                String rangeCounty = rangeDist.getDistCode(COUNTY);
                String rangeTown = rangeDist.getDistCode(TOWN);
                boolean baseCodeValid = baseDist.hasDistrictCode(distType);
                boolean rangeCodeValid = rangeDist.hasDistrictCode(distType);
                boolean isCountyBased = DistrictType.getCountyBasedTypes().contains(distType);
                boolean isTownBased = DistrictType.getTownBasedTypes().contains(distType);

                if ( !(baseCodeValid && rangeCodeValid && rangeCode.equals(baseCode))
                                     || (isCountyBased && (rangeCounty == null || !rangeCounty.equals(baseCounty)))
                                     || (isTownBased && (rangeTown == null || !rangeTown.equals(baseTown)))) {
                    baseDist.setDistCode(distType, null);
                }
            }
        }

        if (baseDist.hasDistrictCode(SENATE)) {
            return baseDist;
        }
        else {
            return null;
        }
    }

    public static class DistrictStreetRangeMapHandler implements ResultSetHandler<Map<StreetAddressRange,DistrictInfo>>
    {
        @Override
        public Map<StreetAddressRange, DistrictInfo> handle(ResultSet rs) throws SQLException
        {
            Map<StreetAddressRange, DistrictInfo> streetRangeMap = new LinkedHashMap<>();
            while (rs.next()) {
                StreetAddressRange sar = new StreetAddressRange();
                DistrictInfo dInfo = new DistrictInfo();

                sar.setId(rs.getInt("id"));
                sar.setBldgLoNum(rs.getInt("bldg_lo_num"));
                sar.setBldgHiNum(rs.getInt("bldg_hi_num"));
                sar.setStreet(rs.getString("street"));
                sar.setLocation(rs.getString("town"));
                sar.setZip5(rs.getString("zip5"));
                sar.setBldgParity(rs.getString("bldg_parity"));

                dInfo.setDistCode(ELECTION, rs.getString(distColMap.get(ELECTION)));
                dInfo.setDistCode(COUNTY, rs.getString(distColMap.get(COUNTY)));
                dInfo.setDistCode(ASSEMBLY, rs.getString(distColMap.get(ASSEMBLY)));
                dInfo.setDistCode(SENATE, rs.getString(distColMap.get(SENATE)));
                dInfo.setDistCode(CONGRESSIONAL, rs.getString(distColMap.get(CONGRESSIONAL)));
                dInfo.setDistCode(TOWN, rs.getString(distColMap.get(TOWN)));
                dInfo.setDistCode(ZIP, rs.getString(distColMap.get(ZIP)));
                dInfo.setDistCode(WARD, rs.getString(distColMap.get(WARD)));
                dInfo.setDistCode(SCHOOL, rs.getString(distColMap.get(SCHOOL)));
                dInfo.setDistCode(CLEG, rs.getString(distColMap.get(CLEG)));
                dInfo.setDistCode(CITY, rs.getString(distColMap.get(CITY)));
                dInfo.setDistCode(VILLAGE, rs.getString(distColMap.get(VILLAGE)));
                dInfo.setDistCode(FIRE, rs.getString(distColMap.get(FIRE)));

                streetRangeMap.put(sar, dInfo);
            }
            return streetRangeMap;
        }
    }
}
